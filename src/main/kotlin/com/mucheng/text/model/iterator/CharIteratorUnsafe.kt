package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.mark.UnsafeApi

@UnsafeApi
class CharIteratorUnsafe(textModel: AbstractTextModel) : CharIterator(textModel) {

    private var index: Int = -1

    override fun hasNext(): Boolean {
        return index + 1 < textModel.length
    }

    override fun nextChar(): Char {
        ++index
        return textModel.getUnsafe(index)
    }

}