package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import kotlin.collections.CharIterator

open class CharIterator(private val textModel: AbstractTextModel) : CharIterator() {

    private var index: Int = -1

    override fun hasNext(): Boolean {
        return index + 1 < textModel.length
    }

    override fun nextChar(): Char {
        ++index
        return textModel[index]
    }

}