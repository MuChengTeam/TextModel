package com.mucheng.text.model.base

import com.mucheng.text.model.standard.TextRow

/**
 * TextModel 接口实现
 * */
interface ITextModel : CharSequence {

    override val length: Int

    override fun get(index: Int): Char

    fun get(column: Int, row: Int): Char

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    fun subSequence(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int): CharSequence

    fun append(charSequence: CharSequence)

    fun insert(column: Int, row: Int, charSequence: CharSequence)

    fun delete(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int)

    fun deleteCharAt(column: Int, row: Int)

    fun charIterator(): CharIterator

    fun textRowIterator(): Iterator<TextRow>

}