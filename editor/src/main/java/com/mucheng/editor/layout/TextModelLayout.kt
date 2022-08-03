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

package com.mucheng.editor.layout

import com.mucheng.editor.base.layout.AbstractLayout
import com.mucheng.editor.view.MuCodeEditor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class TextModelLayout(editor: MuCodeEditor) : AbstractLayout(editor) {

    override fun measureLineRow(line: Int): Float {
        val textModel = editor.getText()
        val textLineModel = textModel.getTextLineModel(line)
        return measureLineRow(line, 0, textLineModel.length)
    }

    override fun measureLineRow(line: Int, startRow: Int, endRow: Int): Float {
        val textModel = editor.getText()
        val textLineModel = textModel.getTextLineModel(line)
        val painters = editor.styleManager.painters

        var widths = 0f
        val widthArray = FloatArray(endRow - startRow)
        painters.codeTextPainter.getTextWidths(textLineModel, startRow, endRow, widthArray)

        for (width in widthArray) {
            widths += width
        }
        return widths
    }

    override fun measureLineRowUsingArray(line: Int): FloatArray {
        val textModel = editor.getText()
        val textLineModel = textModel.getTextLineModel(line)
        return measureLineRowUsingArray(line, 0, textLineModel.length)
    }

    override fun measureLineRowUsingArray(line: Int, startRow: Int, endRow: Int): FloatArray {
        val textModel = editor.getText()
        val textLineModel = textModel.getTextLineModel(line)
        val painters = editor.styleManager.painters

        val widthArray = FloatArray(endRow - startRow)
        painters.codeTextPainter.getTextWidths(textLineModel, startRow, endRow, widthArray)
        return widthArray
    }

    override fun measureAllRowsUsingArray(): Array<FloatArray?> {
        val textModel = editor.getText()
        val lineCount = textModel.lineCount
        val lineArrays = arrayOfNulls<FloatArray>(lineCount)
        var workLine = 1
        while (workLine <= lineCount) {
            lineArrays[workLine - 1] = measureLineRowUsingArray(workLine)
            ++workLine
        }
        return lineArrays
    }

    override fun measureAllRows(): FloatArray {
        val textModel = editor.getText()
        val lineCount = textModel.lineCount
        val lineArrays = FloatArray(lineCount)
        var workLine = 1
        while (workLine <= lineCount) {
            lineArrays[workLine - 1] = measureLineRow(workLine)
            ++workLine
        }
        return lineArrays
    }

    override fun getFirstVisibleLine(): Int {
        val textModel = editor.getText()
        val painters = editor.styleManager.painters
        val lineHeight = painters.getLineHeight()
        val visibleLine = min(editor.getOffsetY() / lineHeight + 1, textModel.lineCount)
        return max(1, visibleLine)
    }

    override fun getLastVisibleLine(): Int {
        val textModel = editor.getText()
        val painters = editor.styleManager.painters
        val lineHeight = painters.getLineHeight()
        val visibleLine =
            min((editor.getOffsetY() + editor.height) / lineHeight + 1, textModel.lineCount)
        return max(1, visibleLine)
    }

    override fun getCursorOffsetLine(offsetY: Float): Int {
        val lineCount = editor.getText().lineCount
        val lineHeight = editor.styleManager.painters.getLineHeight()

        return max(
            1, min(
                (offsetY / lineHeight).toInt() + 1,
                lineCount
            )
        )
    }

    override fun getCursorOffsetRow(line: Int, offsetX: Float): Int {
        val textModel = editor.getText()
        val textLineModel = textModel.getTextLineModel(line)
        val leftToolbarWidth = editor.getLeftToolbarWidth()
        if (offsetX <= leftToolbarWidth) {
            return 0
        }

        val widths = measureLineRowUsingArray(line)

        var workOffsetX = leftToolbarWidth
        val sortedResult = widths.mapIndexed { index, width ->
            workOffsetX += width
            index to abs(workOffsetX - offsetX).toInt()
        }.sortedWith { p1, p2 ->
            p1.second.compareTo(p2.second)
        }
        if (offsetX > workOffsetX) {
            return textLineModel.length
        }
        return max(
            0,
            min(sortedResult.getOrNull(0)?.let { it.first + 1 } ?: 0, textLineModel.length))
    }

    override fun getLayoutWidth(): Int {
        return editor.getRenderMaxX().toInt()
    }

    override fun getLayoutHeight(): Int {
        val textModel = editor.getText()
        val lineCount = textModel.lineCount
        val lineHeight = editor.styleManager.painters.getLineHeight()
        return lineCount * lineHeight
    }

}