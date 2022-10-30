package com.mucheng.text.model.base

import com.mucheng.text.model.standard.TextRow

/**
 * TextModel 接口实现
 * */
interface ITextModel : CharSequence {

    override val length: Int

    override fun get(index: Int): Char

    fun get(line: Int, row: Int): Char

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    fun subSequence(startLine: Int, startRow: Int, endLine: Int, endRow: Int): CharSequence

    fun append(charSequence: CharSequence)

    fun insert(index: Int, charSequence: CharSequence)

    fun insert(line: Int, row: Int, charSequence: CharSequence)

    fun delete(startIndex: Int, endIndex: Int)

    fun delete(startLine: Int, startRow: Int, endLine: Int, endRow: Int)

    fun deleteCharAt(index: Int)

    fun deleteCharAt(line: Int, row: Int)

    fun charIterator(): CharIterator

    fun textRowIterator(): Iterator<TextRow>

}