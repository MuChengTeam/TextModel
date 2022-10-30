package com.mucheng.text.model.standard

import java.nio.CharBuffer

open class CharArrayWrapper(private val array: CharArray, private var _length: Int) : CharSequence {

    fun setLength(length: Int) {
        this._length = length
    }

    override val length: Int
        get() {
            return _length
        }

    override fun get(index: Int): Char {
        return array[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return CharBuffer.wrap(array, startIndex, endIndex - startIndex)
    }

}