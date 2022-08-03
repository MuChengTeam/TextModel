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

package com.mucheng.editor.sample.filter

import com.mucheng.editor.base.IAutoCompletionPublisher
import com.mucheng.editor.base.panel.AbstractAutoCompletionPanel
import com.mucheng.editor.debug.MyLog
import com.mucheng.editor.view.MuCodeEditor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

open class DefaultAutoCompletionPublisher : IAutoCompletionPublisher {

    companion object {
        private const val TAG = "DefaultAutoCompletionPublisher"
    }

    override fun fetchInputText(editor: MuCodeEditor): String {
        val textModel = editor.getText()
        val cursor = editor.getCursor()
        val line = cursor.line
        var workRow = cursor.row
        val textLineModel = textModel.getTextLineModel(line)
        val language = editor.languageManager.language
        val cache = StringBuilder()

        while (workRow > 0) {
            --workRow

            val char = textLineModel[workRow]
            if (language.isOperator(char) || language.isWhitespace(char) || language.isDigit(
                    char
                )
            ) {
                break
            }

            if (cache.isEmpty()) {
                cache.append(char)
                continue
            }

            cache.insert(0, char)
        }

        return cache.toString()
    }

    @Suppress("BlockingMethodInNonBlockingContext", "ControlFlowWithEmptyBody")
    override suspend fun publish(
        editor: MuCodeEditor,
        autoCompletionPanel: AbstractAutoCompletionPanel,
    ) {
        coroutineScope {
            val textModel = editor.getText()
            val cursor = editor.getCursor()
            val line = cursor.line
            var workRow = cursor.row
            val textLineModel = textModel.getTextLineModel(line)
            val language = editor.languageManager.language
            val cursorAnimation = editor.animationManager.cursorAnimation
            val cache = StringBuilder()
            val autoCompletionItems = autoCompletionPanel.getCustomCompletionItems()

            if (language.getLexer() == null) {
                return@coroutineScope
            }

            while (workRow > 0 && isActive) {
                --workRow

                val char = textLineModel[workRow]
                if (language.isOperator(char) || language.isWhitespace(char) || language.isDigit(
                        char
                    )
                ) {
                    break
                }

                if (cache.isEmpty()) {
                    cache.append(char)
                    continue
                }

                cache.insert(0, char)
            }

            if (!isActive) {
                MyLog.e(TAG, "Cancelled")
                return@coroutineScope
            }

            if (cache.isEmpty()) {
                autoCompletionPanel.dismiss()
                return@coroutineScope
            }

            val matchItems = autoCompletionItems.filter { it.title.startsWith(cache) }.toList()
            if (matchItems.isEmpty()) {
                autoCompletionPanel.dismiss()
                return@coroutineScope
            }

            autoCompletionPanel.clearCustomCompletionItems()

            for (matchItem in matchItems) {
                autoCompletionPanel.addCustomCompletionItems(matchItem)
            }

            // 为了防止卡顿，如果光标动画在运行的话则堵塞协程
            while (cursorAnimation.isRunning()) {
            }

            editor.post {
                autoCompletionPanel.notifyAutoCompletionItemChanged()
                autoCompletionPanel.show()
            }
        }
    }

}