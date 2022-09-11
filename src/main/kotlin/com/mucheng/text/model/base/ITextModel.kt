package com.mucheng.text.model.base

import com.mucheng.text.model.standard.TextRow

/**
 * TextModel 接口实现
 * */
interface ITextModel : CharSequence {

    override val length: Int

    val lastIndex: Int

    val lastLine: Int

    override fun get(index: Int): Char

    fun get(line: Int, row: Int): Char

    fun getTextRow(line: Int): TextRow

    fun getTextRowSize(line: Int): Int

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    fun subSequence(startLine: Int, startRow: Int, endLine: Int, endRow: Int): CharSequence

    fun append(charSequence: CharSequence)

    fun insert(index: Int, charSequence: CharSequence)

    fun insert(line: Int, row: Int, charSequence: CharSequence)

    fun delete(startIndex: Int, endIndex: Int)

    fun delete(startLine: Int, startRow: Int, endLine: Int, endRow: Int)

    fun deleteCharAt(index: Int)

    fun deleteCharAt(line: Int, row: Int)

    fun indexOf(text: CharSequence, startIndex: Int = 0): Int

    fun lastIndexOf(text: CharSequence, startIndex: Int = length): Int

    fun charIterator(): CharIterator

    fun textRowIterator(): Iterator<TextRow>

}