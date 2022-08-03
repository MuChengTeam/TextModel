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

package com.mucheng.editor.sample.component

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.mucheng.editor.R
import com.mucheng.editor.base.panel.AbstractAutoCompletionPanel
import com.mucheng.editor.data.AutoCompletionItem
import com.mucheng.editor.tool.dp
import com.mucheng.editor.view.MuCodeEditor
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock

open class DefaultAutoCompletionPanel(editor: MuCodeEditor) : AbstractAutoCompletionPanel(editor) {

    private val inflater by lazy { LayoutInflater.from(editor.context) }

    private val adapter by lazy { Adapter() }

    private lateinit var content: View

    private lateinit var indicator: LinearProgressIndicator

    private val requireAutoCompletionPanelBaseCoroutine =
        CoroutineScope(Dispatchers.IO + CoroutineName("RequireAutoCompletionPanelCoroutine"))

    private var job: Job? = null

    private val lock = ReentrantLock()

    init {
        animationStyle = com.google.android.material.R.style.Animation_AppCompat_Dialog
        isFocusable = false
        isTouchable = true

        width = editor.context.resources.displayMetrics.widthPixels
        height = editor.context.dp(140f).toInt()

        createContentView()
        contentView = content
    }

    override fun onCreateBackground(): Drawable? {
        return null
    }

    override fun updateTheme() {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyAutoCompletionItemChanged() {
        editor.post {
            adapter.notifyDataSetChanged()
        }
    }

    override fun launchRequireAutoCompletionCoroutine() {
        job?.cancel()
        job = requireAutoCompletionPanelBaseCoroutine.launch {
            lock.lock()
            try {
                editor.post {
                    indicator.visibility = View.VISIBLE
                }
                clearCustomCompletionItems()

                val language = editor.getLanguage()
                val keywords = language.getKeywordTokenMap().keys
                val specials = language.getSpecialTokenMap().keys

                for (keyword in keywords) {
                    addCustomCompletionItems(AutoCompletionItem(keyword, KEYWORD))
                    if (!isActive) {
                        return@launch
                    }
                }

                for (special in specials) {
                    addCustomCompletionItems(AutoCompletionItem(special, SPECIAL))
                    if (!isActive) {
                        return@launch
                    }
                }

                if (getCompletionItemSize() == 0) {
                    return@launch
                }

                val filter = getAutoCompletionPublisher()
                filter.publish(editor, this@DefaultAutoCompletionPanel)
                editor.post {
                    indicator.visibility = View.GONE
                }
            } finally {
                lock.unlock()
            }
        }
    }

    override fun show() {
        if (getCompletionItemSize() == 0 || isShowing) {
            return
        }

        super.show()
    }

    @SuppressLint("InflateParams")
    private fun createContentView() {
        content = inflater.inflate(R.layout.layout_auto_completion_panel_content, null, false)
        indicator = content.findViewById(R.id.indicator)

        val recyclerView: RecyclerView = content.findViewById(R.id.autoCompletionRecyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(editor.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    open inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                inflater.inflate(R.layout.layout_auto_completion_panel_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val autoCompletionItem = getCustomCompletionItem(position)
            val autoCompletionHelper = getAutoCompletionHelper()
            holder.itemView.setOnClickListener {
                autoCompletionHelper.insertedText(
                    autoCompletionItem,
                    editor,
                    this@DefaultAutoCompletionPanel,
                    getAutoCompletionPublisher().fetchInputText(editor)
                )
            }
            holder.icon.setImageDrawable(
                autoCompletionHelper.getIconDrawable(
                    editor,
                    autoCompletionItem.type
                )
            )
            holder.title.text = autoCompletionItem.title
            holder.type.text = autoCompletionItem.type
        }

        override fun getItemCount(): Int {
            return getCompletionItemSize()
        }

    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ShapeableImageView = itemView.findViewById(R.id.item_icon)
        val title: MaterialTextView = itemView.findViewById(R.id.item_title)
        val type: MaterialTextView = itemView.findViewById(R.id.item_type)
    }

}