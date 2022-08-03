/*
 * CN:
 * 作者：SuMuCheng
 * 我的 QQ: 3578557729
 * Github 主页：https://github.com/CaiMuCheng
 * 项目主页: https://github.com/CaiMuCheng/MuCodeEditor
 *
 * 你可以免费使用、商用以下代码，也可以基于以下代码做出修改，但是必须在你的项目中标注出处
 * 例如：在你 APP 的设置中添加 “关于编辑器” 一栏，其中标注作者以及此编辑器的 Github 主页
 *
 * 此代码使用 MPL 2.0 开源许可证，你必须标注作者信息
 * 若你要修改文件，请勿删除此注释
 * 若你违反以上条例我们有权向您提起诉讼!
 *
 * EN:
 * Author: SuMuCheng
 * My QQ-Number: 3578557729
 * Github Homepage: https://github.com/CaiMuCheng
 * Project Homepage: https://github.com/CaiMuCheng/MuCodeEditor
 *
 * You can use the following code for free, commercial use, or make modifications based on the following code, but you must mark the source in your project.
 * For example: add an "About Editor" column in your app's settings, which identifies the author and the Github home page of this editor.
 *
 * This code uses the MPL 2.0 open source license, you must mark the author information.
 * Do not delete this comment if you want to modify the file.
 * If you violate the above regulations we have the right to sue you!
 */

package com.mucheng.editor.language.html

import com.mucheng.editor.base.lexer.AbstractLexer

open class HtmlLexer(language: HtmlLanguage) : AbstractLexer(language) {

    override fun analyze() {
        if (line > lineSize()) {
            return
        }

        while (isRunning()) {

            if (line > lineSize()) {
                return
            }

            scannedLineSource = sources.getTextLineModel(line)

            if (row >= rowSize()) {
                ++line
                row = 0
                continue
            }

            scannedLineSource = sources.getTextLineModel(line)
            getChar()

            if (handleWhitespace()) continue

            if (handleComments()) continue

            if (handleString()) continue

            if (handleDOCTYPE()) continue

            if (handleSymbol()) continue

            if (handleElement()) continue

            if (handleAttribute()) continue

            if (handleDigit()) continue

            if (handleIdentifier()) continue

            ++row
        }
    }

    private fun handleString(): Boolean {
        if (scannedChar != '"') {
            return false
        }

        val start = row
        yyChar()

        while (isNotRowEOF()) {
            if (scannedChar == '"') {
                yyChar()
                break
            }
            yyChar()
        }

        val end = row
        addToken(
            HtmlToken.STRING,
            start,
            end
        )
        return true
    }

    private fun handleDOCTYPE(): Boolean {
        if (scannedChar != '<') {
            return false
        }

        val start = row
        yyChar()
        if (scannedChar != '!') {
            row = start
            getChar()
            return false
        }

        val buffer = StringBuilder()
        yyChar()
        while (isLetter() && isNotRowEOF()) {
            buffer.append(scannedChar)
            yyChar()
        }

        val docText = buffer.toString()
        if (docText == "DOCTYPE" || docText == "doctype") {
            yyChar()
            while (isWhitespace() && isNotRowEOF()) {
                yyChar()
            }
            buffer.clear()
            while (isLetter() && isNotRowEOF()) {
                buffer.append(scannedChar)
                yyChar()
            }
            val htmlText = buffer.toString()
            if (htmlText == "html" && scannedChar == '>') {
                val end = ++row
                addToken(
                    HtmlToken.DOCTYPE,
                    start,
                    end
                )
                return true
            }
        }
        row = start
        getChar()
        return false
    }

    private fun handleSymbol(): Boolean {
        if (!isOperator()) {
            return false
        }

        val token = language.getOperatorTokenMap()[scannedChar]!!
        addToken(
            token,
            row,
            row + 1
        )

        ++row
        return true
    }

    private fun handleElement(): Boolean {
        if (isLetter() && scannedLineSource.getOrNull(row - 1) == '<') {
            val start = row
            while ((isLetter() || isDigit()) && isNotRowEOF()) {
                yyChar()
            }
            val end = row
            addToken(
                HtmlToken.ELEMENT_NAME,
                start,
                end
            )
            return true
        }

        if (isLetter() && scannedLineSource.getOrNull(row - 1) == '/') {
            val start = row
            while ((isLetter() || isDigit()) && isNotRowEOF()) {
                yyChar()
            }
            val end = row
            addToken(
                HtmlToken.ELEMENT_NAME,
                start,
                end
            )
            return true
        }
        return false
    }

    private fun handleAttribute(): Boolean {
        if (!isLetter()) {
            return false
        }

        val start = row
        while ((isLetter() || isDigit()) && isNotRowEOF()) {
            yyChar()
        }
        if (scannedChar == '=') {
            addToken(
                HtmlToken.ATTRIBUTE,
                start,
                row
            )
            return true
        }
        row = start
        getChar()
        return false
    }

    private fun handleDigit(): Boolean {
        if (!isDigit()) {
            return false
        }

        val start = row
        while (isDigit() && isNotRowEOF()) {
            yyChar()
        }
        val end = row

        addToken(
            HtmlToken.IDENTIFIER,
            start,
            end
        )
        return true
    }

    private fun handleIdentifier(): Boolean {
        if (isWhitespace() || isOperator() || isDigit()) {
            return false
        }

        val start = row
        while (!isWhitespace() && !isOperator() && isNotRowEOF()) {
            yyChar()
        }
        val end = row

        addToken(
            HtmlToken.IDENTIFIER,
            start,
            end
        )
        return true
    }

    private fun handleWhitespace(): Boolean {
        if (!isWhitespace()) {
            return false
        }

        val start = row
        while (isWhitespace() && isNotRowEOF()) {
            yyChar()
        }
        val end = row
        addToken(
            HtmlToken.WHITESPACE,
            start,
            end
        )
        return true
    }

    private fun handleComments(): Boolean {
        if (scannedChar != '<') {
            return false
        }
        val start = row
        yyChar()
        if (scannedChar == '!') {
            yyChar()
            if (scannedChar == '-') {
                yyChar()
                if (scannedChar == '-') {
                    val currentFindPos = scannedLineSource.indexOf("-->", row + 1)
                    if (currentFindPos != -1) {
                        val end = currentFindPos + 3
                        addToken(
                            HtmlToken.COMMENT,
                            start,
                            end
                        )
                        row = end
                        return true
                    }
                    addToken(
                        HtmlToken.COMMENT,
                        start,
                        rowSize()
                    )

                    ++line
                    while (line <= lineSize()) {
                        row = 0
                        scannedLineSource = sources.getTextLineModel(line)
                        if (scannedLineSource.isEmpty()) {
                            ++line
                            continue
                        }

                        val findPos = scannedLineSource.indexOf("-->")
                        if (findPos != -1) {
                            val end = findPos + 3
                            addToken(
                                HtmlToken.COMMENT,
                                0,
                                end
                            )
                            row = end
                            return true
                        }

                        addToken(
                            HtmlToken.COMMENT,
                            0,
                            rowSize()
                        )
                        ++line
                    }
                    return true
                }
            }
        }

        row = start
        getChar()
        return false
    }

}