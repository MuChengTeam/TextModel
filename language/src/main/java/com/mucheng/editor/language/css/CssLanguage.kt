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

package com.mucheng.editor.language.css

import com.mucheng.editor.base.lang.AbstractBasicLanguage
import com.mucheng.editor.token.ThemeToken
import com.mucheng.editor.view.MuCodeEditor

object CssLanguage : AbstractBasicLanguage() {

    private lateinit var editor: MuCodeEditor

    private val lexer = CssLexer(this)

    private val operatorTokenMap: Map<Char, ThemeToken> = hashMapOf(
        '+' to CssToken.PLUS,
        '*' to CssToken.MULTI,
        '/' to CssToken.DIV,
        ':' to CssToken.COLON,
        '!' to CssToken.NOT,
        '%' to CssToken.MOD,
        '^' to CssToken.XOR,
        '&' to CssToken.AND,
        '?' to CssToken.QUESTION,
        '~' to CssToken.COMP,
        '.' to CssToken.DOT,
        ',' to CssToken.COMMA,
        ';' to CssToken.SEMICOLON,
        '=' to CssToken.EQUALS,
        '(' to CssToken.LEFT_PARENTHESIS,
        ')' to CssToken.RIGHT_PARENTHESIS,
        '[' to CssToken.LEFT_BRACKET,
        ']' to CssToken.RIGHT_BRACKET,
        '{' to CssToken.LEFT_BRACE,
        '}' to CssToken.RIGHT_BRACE,
        '|' to CssToken.OR,
        '<' to CssToken.LESS_THAN,
        '>' to CssToken.MORE_THAN
    )

    private val keywordTokenMap: Map<String, ThemeToken> = emptyMap()

    private val specialTokenMap: Map<String, ThemeToken> = emptyMap()

    override fun getLexer(): CssLexer {
        return lexer
    }

    override fun doSpan(): Boolean {
        return true
    }

    override fun setEditor(editor: MuCodeEditor) {
        this.editor = editor
    }

    override fun getEditor(): MuCodeEditor {
        return editor
    }

    override fun getOperatorTokenMap(): Map<Char, ThemeToken> {
        return operatorTokenMap
    }

    override fun getKeywordTokenMap(): Map<String, ThemeToken> {
        return keywordTokenMap
    }

    override fun getSpecialTokenMap(): Map<String, ThemeToken> {
        return specialTokenMap
    }

}