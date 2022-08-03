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

package com.mucheng.sample

import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.mucheng.editor.animation.CursorFadeAnimation
import com.mucheng.editor.animation.CursorScaleAnimation
import com.mucheng.editor.animation.CursorTranslationAnimation
import com.mucheng.editor.event.ContentChangedEvent
import com.mucheng.editor.language.css.CssLanguage
import com.mucheng.editor.language.ecmascript.EcmaScriptLanguage
import com.mucheng.editor.language.html.HtmlLanguage
import com.mucheng.editor.sample.language.TextLanguage
import com.mucheng.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "MainActivity"

    }

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var undoMenuItem: MenuItem

    private lateinit var redoMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)

        val editor = viewBinding.editor
        editor.styleManager.setTypeface(
            Typeface.createFromAsset(
                assets,
                "font/HarmonyOS-Sans-Regular.ttf"
            )
        )
        editor.languageManager.setLanguage(EcmaScriptLanguage)
        editor.open(assets.open("text/sample-ecmascript.js"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        undoMenuItem = menu.findItem(R.id.undo)
        redoMenuItem = menu.findItem(R.id.redo)
        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val editor = viewBinding.editor
        editor.eventManager.subscribeEvent(object : ContentChangedEvent {

            override fun onContentChanged() {
                undoMenuItem.isEnabled = editor.canUndo()
                redoMenuItem.isEnabled = editor.canRedo()
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val editor = viewBinding.editor
        when (item.itemId) {

            R.id.undo -> {
                editor.undo()
                undoMenuItem.isEnabled = editor.canUndo()
                redoMenuItem.isEnabled = editor.canRedo()
            }

            R.id.redo -> {
                editor.redo()
                undoMenuItem.isEnabled = editor.canUndo()
                redoMenuItem.isEnabled = editor.canRedo()
            }

            R.id.translation_animation -> {
                editor.animationManager.setCursorAnimation(CursorTranslationAnimation(editor))
            }

            R.id.scale_animation -> {
                editor.animationManager.setCursorAnimation(CursorScaleAnimation(editor))
            }

            R.id.fade_animation -> {
                editor.animationManager.setCursorAnimation(CursorFadeAnimation(editor))
            }

            R.id.cursor_to_home -> {
                editor.moveCursorToHome()
            }

            R.id.cursor_to_end -> {
                editor.moveCursorToEnd()
            }

            R.id.cursor_to_line_start -> {
                editor.moveCursorToLineStart()
            }

            R.id.cursor_to_line_end -> {
                editor.moveCursorToLineEnd()
            }

            R.id.cursor_to_top -> {
                editor.moveCursorToTop()
            }

            R.id.cursor_to_bottom -> {
                editor.moveCursorToBottom()
            }

            R.id.cursor_to_left -> {
                editor.moveCursorToLeft()
            }

            R.id.cursor_to_right -> {
                editor.moveCursorToRight()
            }

            R.id.selection_all -> {
                editor.selectionAll()
            }

            R.id.switch_theme -> {
                editor.setDarkTheme(!editor.isDarkTheme())
            }

            R.id.visible_animation -> {
                editor.functionManager.setCursorVisibleAnimationEnabled(!editor.functionManager.isCursorVisibleAnimationEnabled)
            }

            R.id.lineNumber_visible -> {
                editor.functionManager.setLineNumberEnabled(!editor.functionManager.isLineNumberEnabled)
                editor.invalidate()
            }

            R.id.editor_mode -> {
                editor.functionManager.setEditEnabled(!editor.functionManager.isEditable)
                editor.hideSoftInput()
                editor.invalidate()
            }

            R.id.action_instance_ecmascript -> {
                editor.open(assets.open("text/sample-ecmascript.js"))
            }

            R.id.action_instance_java -> {
                editor.open(assets.open("text/SampleJava.java"))
            }

            R.id.action_instance_html -> {
                editor.open(assets.open("text/sample-html.html"))
            }

            R.id.action_instance_css -> {
                editor.open(assets.open("text/sample-css.css"))
            }

            R.id.test_vuejs -> {
                editor.open(assets.open("capacity_test/vue.js"))
            }

            R.id.none_highlight -> {
                editor.setLanguage(TextLanguage)
            }

            R.id.highlight_ecmascript -> {
                editor.setLanguage(EcmaScriptLanguage)
            }

            R.id.highlight_html -> {
                editor.setLanguage(HtmlLanguage)
            }

            R.id.highlight_css -> {
                editor.setLanguage(CssLanguage)
            }

        }
        return true
    }

}