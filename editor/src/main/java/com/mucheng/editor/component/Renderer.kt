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

package com.mucheng.editor.component

import android.graphics.Bitmap
import android.graphics.Canvas
import com.mucheng.editor.base.AbstractComponent
import com.mucheng.editor.base.AbstractTheme
import com.mucheng.editor.base.layout.AbstractLayout
import com.mucheng.editor.token.ThemeToken
import com.mucheng.editor.tool.dp
import com.mucheng.editor.view.MuCodeEditor
import com.mucheng.storage.model.model.TextModel
import kotlin.math.max

@Suppress("MemberVisibilityCanBePrivate")
open class Renderer(editor: MuCodeEditor) : AbstractComponent(editor) {

    companion object {

        private const val TAG = "Renderer"

    }

    var maxWidth = 0f
        private set

    private val margin = editor.context.dp(5)

    protected lateinit var theme: AbstractTheme

    protected lateinit var painters: Painters

    protected lateinit var textModel: TextModel

    protected lateinit var layout: AbstractLayout

    protected var offsetX = margin * 2

    protected var offsetY = 0f

    var leftToolbarWidth = 0f
        protected set

    val textSelectHandleStartParams = FloatArray(4)

    val textSelectHandleEndParams = FloatArray(4)

    private fun remake() {
        offsetX = margin * 2
        offsetY = 0f

        theme = editor.styleManager.theme
        painters = editor.styleManager.painters
        textModel = editor.getText()
        layout = editor.layout

        painters.lineNumberPainter.color =
            theme.getColor(ThemeToken.LINE_NUMBER_COLOR_TOKEN).hexColor

        if (!editor.animationManager.cursorAnimation.isRunning()) {
            painters.cursorPainter.color = theme.getColor(ThemeToken.CURSOR_COLOR_TOKEN).hexColor
        }

        painters.textSelectHandleBackgroundPainter.color =
            theme.getColor(ThemeToken.TEXT_SELECT_HANDLE_BACKGROUND_COLOR_TOKEN).hexColor

        painters.textSelectHandlePainter.color =
            theme.getColor(ThemeToken.TEXT_SELECT_HANDLE_COLOR_TOKEN).hexColor
    }

    open fun render(canvas: Canvas) {
        remake()
        renderBackgroundColor(canvas)
        renderBackground(canvas)
        renderLineNumber(canvas)
        renderTextSelectHandleBackground(canvas)
        renderTextSelectHandle(canvas)
        renderCodeText(canvas)
        renderCursor(canvas)
    }

    protected open fun renderBackgroundColor(canvas: Canvas) {
        val backgroundColor = theme.getColor(ThemeToken.BACKGROUND_COLOR_TOKEN).hexColor
        canvas.drawColor(backgroundColor)
    }

    protected open fun renderBackground(canvas: Canvas) {
        if (!editor.functionManager.isCustomBackgroundEnabled) {
            return
        }

        val destBitmap = editor.styleManager.customBackground ?: return
        if (destBitmap.width != editor.width || destBitmap.height != editor.height) {
            editor.styleManager.setCustomBackground(
                Bitmap.createScaledBitmap(
                    destBitmap,
                    editor.width,
                    editor.height,
                    true
                )
            )
        }
        canvas.drawBitmap(
            editor.styleManager.customBackground!!,
            0f,
            0f,
            painters.customBackgroundPainter
        )
    }

    protected open fun renderLineNumber(canvas: Canvas) {
        if (!editor.functionManager.isLineNumberEnabled) {
            offsetX = 0f
            leftToolbarWidth = 0f
            return
        }
        val lineHeight = painters.getLineHeight()
        var workLine = editor.getFirstVisibleLine()
        val reachLine = editor.getLastVisibleLine()
        val scrollingOffsetX = editor.getOffsetX()
        val scrollingOffsetY = editor.getOffsetY()
        while (workLine <= reachLine) {
            canvas.drawText(
                workLine.toString(), offsetX - scrollingOffsetX,
                (lineHeight * workLine).toFloat() - scrollingOffsetY, painters.lineNumberPainter
            )
            ++workLine
        }
        offsetX += painters.lineNumberPainter.measureText(reachLine.toString())
        leftToolbarWidth = offsetX + margin * 2
    }

    protected open fun renderCodeText(canvas: Canvas) {
        val lineHeight = painters.getLineHeight()
        var workLine = editor.getFirstVisibleLine()
        val reachLine = editor.getLastVisibleLine()
        val painter = painters.codeTextPainter
        val scrollingOffsetX = editor.getOffsetX()
        val scrollingOffsetY = editor.getOffsetY()
        val languageManager = editor.languageManager
        val styleManager = editor.styleManager
        val language = languageManager.language
        val spans = styleManager.spans

        val x = offsetX - scrollingOffsetX


        if (language.doSpan()) {
            try {
                while (workLine <= reachLine) {
                    val textLineModel = textModel.getTextLineModel(workLine)
                    val lineSpans = spans.getLineSpan(workLine)
                    var offsetX = x
                    val y = (workLine * lineHeight).toFloat() - scrollingOffsetY
                    for (span in lineSpans) {
                        painter.color = span.color.hexColor
                        canvas.drawText(
                            textLineModel,
                            span.startRow,
                            span.endRow,
                            offsetX,
                            y,
                            painter
                        )
                        offsetX += layout.measureLineRow(workLine, span.startRow, span.endRow)
                    }
                    maxWidth = max(maxWidth, offsetX)
                    ++workLine
                }

            } catch (e: IndexOutOfBoundsException) {
                painter.color = theme.getColor(ThemeToken.IDENTIFIER_COLOR_TOKEN).hexColor
                while (workLine <= reachLine) {
                    val textLineModel = textModel.getTextLineModel(workLine)
                    val y = (workLine * lineHeight).toFloat() - scrollingOffsetY

                    canvas.drawText(textLineModel, 0, textLineModel.length, x, y, painter)
                    maxWidth = max(maxWidth, layout.measureLineRow(workLine))
                    ++workLine
                }
            }
        } else {
            painter.color = theme.getColor(ThemeToken.IDENTIFIER_COLOR_TOKEN).hexColor
            while (workLine <= reachLine) {
                val textLineModel = textModel.getTextLineModel(workLine)
                val y = (workLine * lineHeight).toFloat() - scrollingOffsetY

                canvas.drawText(textLineModel, 0, textLineModel.length, x, y, painter)
                maxWidth = max(maxWidth, layout.measureLineRow(workLine))
                ++workLine
            }
        }
    }

    protected open fun renderCursor(canvas: Canvas) {
        if (!editor.isEnabled || !editor.functionManager.isEditable || editor.actionManager.selectingText) {
            return
        }
        val cursor = editor.getCursor()
        val lineHeight = painters.getLineHeight()
        val painter = painters.cursorPainter
        val visibleLineStart = editor.getFirstVisibleLine()
        val visibleLineEnd = editor.getLastVisibleLine()
        val scrollingOffsetX = editor.getOffsetX()
        val scrollingOffsetY = editor.getOffsetY()
        if (cursor.line < visibleLineStart || cursor.line > visibleLineEnd) {
            return
        }

        val cursorAnimation = editor.animationManager.cursorAnimation
        if (cursorAnimation.isRunning()) {
            val animatedX = offsetX + cursorAnimation.animatedX() - scrollingOffsetX
            val animatedY = cursorAnimation.animatedY()
            val startY = animatedY - lineHeight / 1.5f - scrollingOffsetY
            val endY = animatedY + lineHeight / 9 - scrollingOffsetY
            canvas.drawLine(
                animatedX,
                startY,
                animatedX,
                endY,
                painter
            )
            return
        }

        val cursorVisibleAnimation = editor.animationManager.cursorVisibleAnimation
        if (cursorVisibleAnimation.isRunning()) {
            if (!cursorVisibleAnimation.isVisible()) {
                return
            }
        }

        val cursorOffsetX = offsetX + getCursorOffsetX(cursor) - scrollingOffsetX
        val cursorOffsetTopY = getCursorOffsetTopY(cursor)
        val startY = cursorOffsetTopY - lineHeight / 1.5f - scrollingOffsetY
        val endY = cursorOffsetTopY + lineHeight / 9 - scrollingOffsetY

        canvas.drawLine(
            cursorOffsetX,
            startY,
            cursorOffsetX,
            endY,
            painter
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getCursorOffsetX(cursor: Cursor): Float {
        if (cursor.row == 0) {
            return 0f
        }

        return layout.measureLineRow(cursor.line, 0, cursor.row)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getCursorOffsetTopY(cursor: Cursor): Float {
        return (painters.getLineHeight() * cursor.line).toFloat()
    }

    protected open fun renderTextSelectHandleBackground(canvas: Canvas) {
        val actionManager = editor.actionManager
        if (editor.functionManager.isLineNumberEnabled) {
            offsetX += margin * 2
        }

        if (!actionManager.selectingText) {
            return
        }

        val selectionRange = actionManager.selectingRange!!
        val startPos = selectionRange.start
        val endPos = selectionRange.end
        val startLine = startPos.line
        val endLine = endPos.line
        val visibleLineStart = editor.getFirstVisibleLine()
        val visibleLineEnd = editor.getLastVisibleLine()
        val scrollingOffsetX = editor.getOffsetX()
        val scrollingOffsetY = editor.getOffsetY()
        val painter = painters.textSelectHandleBackgroundPainter

        val fontMetricsInt = painters.codeTextPainter.fontMetricsInt
        val fontMetricsOffset = fontMetricsInt.descent
        val realOffsetX = offsetX - scrollingOffsetX
        val realOffsetY = offsetY - scrollingOffsetY + fontMetricsOffset
        val lineHeight = painters.getLineHeight()
        if (startLine == endLine) {
            val startX = layout.measureLineRow(startLine, 0, startPos.row) + realOffsetX
            val startY = (startLine - 1) * lineHeight + realOffsetY
            val endX = layout.measureLineRow(endLine, 0, endPos.row) + realOffsetX
            val endY = endLine * lineHeight + realOffsetY
            canvas.drawRect(
                startX,
                startY,
                endX,
                endY,
                painter
            )
            return
        }

        var workLine = if (startLine < visibleLineStart) visibleLineStart else startLine
        val reachLine = if (endLine > visibleLineEnd) visibleLineEnd else endLine
        while (workLine <= reachLine) {
            var endX: Float
            var endY: Float
            when (workLine) {
                startLine -> {
                    val startTextLineModel = textModel.getTextLineModel(workLine)
                    val startX = layout.measureLineRow(workLine, 0, startPos.row) + realOffsetX
                    endX = layout.measureLineRow(
                        workLine,
                        startPos.row,
                        startTextLineModel.length
                    ) + startX
                    endY = (workLine * lineHeight).toFloat() + realOffsetY
                    canvas.drawRect(
                        startX,
                        endY - lineHeight,
                        endX,
                        endY,
                        painter
                    )
                    ++workLine
                    continue
                }

                endLine -> {
                    endX = layout.measureLineRow(workLine, 0, endPos.row) + realOffsetX
                    endY = (workLine * lineHeight).toFloat() + realOffsetY
                }

                else -> {
                    endX = layout.measureLineRow(workLine) + realOffsetX
                    endY = (workLine * lineHeight).toFloat() + realOffsetY
                }
            }
            endX = max(endX, realOffsetX + 20)
            canvas.drawRect(
                realOffsetX,
                endY - lineHeight,
                endX,
                endY,
                painter
            )
            ++workLine
        }
    }

    protected open fun renderTextSelectHandle(canvas: Canvas) {
        val actionManager = editor.actionManager
        if (!actionManager.selectingText) {
            return
        }

        val selectionRange = actionManager.selectingRange!!
        val startPos = selectionRange.start
        val endPos = selectionRange.end
        val lineHeight = painters.getLineHeight()
        val textSelectHandle = editor.textSelectHandle
        val scrollingOffsetX = editor.getOffsetX()
        val scrollingOffsetY = editor.getOffsetY()
        val painter = painters.textSelectHandlePainter

        val startX =
            offsetX + layout.measureLineRow(startPos.line, 0, startPos.row) - scrollingOffsetX
        val startY = startPos.line * lineHeight.toFloat() - scrollingOffsetY
        textSelectHandle.draw(
            canvas,
            startX,
            startY,
            painter,
            TextSelectHandle.LEFT_TEXT_SELECT_HANDLE
        )
        textSelectHandleStartParams[0] = textSelectHandle.startX
        textSelectHandleStartParams[1] = textSelectHandle.startY
        textSelectHandleStartParams[2] = textSelectHandle.endX
        textSelectHandleStartParams[3] = textSelectHandle.endY

        val endX =
            offsetX + layout.measureLineRow(endPos.line, 0, endPos.row) - scrollingOffsetX
        val endY = endPos.line * lineHeight.toFloat() - scrollingOffsetY
        textSelectHandle.draw(
            canvas,
            endX,
            endY,
            painter,
            TextSelectHandle.RIGHT_TEXT_SELECT_HANDLE
        )
        textSelectHandleEndParams[0] = textSelectHandle.startX
        textSelectHandleEndParams[1] = textSelectHandle.startY
        textSelectHandleEndParams[2] = textSelectHandle.endX
        textSelectHandleEndParams[3] = textSelectHandle.endY
    }


}