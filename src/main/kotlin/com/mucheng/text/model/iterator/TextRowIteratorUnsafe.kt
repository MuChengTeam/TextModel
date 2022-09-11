package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.mark.UnsafeApi
import com.mucheng.text.model.standard.TextRow

@UnsafeApi
class TextRowIteratorUnsafe(textModel: AbstractTextModel) : TextRowIterator(textModel) {

    private var line = 0

    override fun hasNext(): Boolean {
        return line + 1 <= textModel.lastLine
    }

    override fun next(): TextRow {
        ++line
        return textModel.getTextRowUnsafe(line)
    }

}