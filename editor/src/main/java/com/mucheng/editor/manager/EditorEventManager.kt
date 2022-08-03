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

package com.mucheng.editor.manager

import com.mucheng.annotations.mark.Model
import com.mucheng.editor.base.AbstractManager
import com.mucheng.editor.event.*
import com.mucheng.editor.view.MuCodeEditor

@Model
open class EditorEventManager(editor: MuCodeEditor) : AbstractManager(editor) {

    private val sizeChangedEvents: MutableList<SizeChangedEvent> = ArrayList()

    private val selectionChangedEvents: MutableList<SelectionChangedEvent> = ArrayList()

    private val selectionStateChangedEvents: MutableList<SelectionStateChangedEvent> = ArrayList()

    private val renderEvents: MutableList<RenderEvent> = ArrayList()

    private val contentChangedEvents: MutableList<ContentChangedEvent> = ArrayList()

    open fun subscribeEvent(event: Event): EditorEventManager {
        when (event) {
            is RenderEvent -> renderEvents.add(event)
            is SelectionChangedEvent -> selectionChangedEvents.add(event)
            is SelectionStateChangedEvent -> selectionStateChangedEvents.add(event)
            is SizeChangedEvent -> sizeChangedEvents.add(event)
            is ContentChangedEvent -> contentChangedEvents.add(event)
        }
        return this
    }

    open fun unregisterEvent(event: Event): EditorEventManager {
        when (event) {
            is RenderEvent -> renderEvents.remove(event)
            is SelectionChangedEvent -> selectionChangedEvents.remove(event)
            is SelectionStateChangedEvent -> selectionStateChangedEvents.remove(event)
            is SizeChangedEvent -> sizeChangedEvents.remove(event)
            is ContentChangedEvent -> contentChangedEvents.remove(event)
        }
        return this
    }

    open fun dispatchRenderBeginEvent(): EditorEventManager {
        renderEvents.forEach {
            it.onRenderBegin()
        }
        return this
    }

    open fun dispatchRenderFinishEvent(): EditorEventManager {
        renderEvents.forEach {
            it.onRenderFinish()
        }
        return this
    }

    open fun dispatchSelectionChangedEvent() {
        selectionChangedEvents.forEach {
            it.onSelectionChanged()
        }
    }

    open fun dispatchSelectionStateChangedEvent(state: String) {
        selectionStateChangedEvents.forEach {
            it.onSelectionStateChanged(state)
        }
    }

    open fun dispatchSizeChangedEvent(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ): EditorEventManager {
        sizeChangedEvents.forEach {
            it.onSizeChanged(width, height, oldWidth, oldHeight)
        }
        return this
    }

    open fun dispatchContentChangedEvent() {
        contentChangedEvents.forEach {
            it.onContentChanged()
        }
    }

}