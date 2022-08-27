package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.mark.UnsafeApi
import com.mucheng.text.model.standard.TextRow

@UnsafeApi
class TextRowIteratorUnsafe(textModel: AbstractTextModel) : TextRowIterator(textModel) {

    private var column = 0

    override fun hasNext(): Boolean {
        return column + 1 <= textModel.lastColumn
    }

    override fun next(): TextRow {
        ++column
        return textModel.getTextRowUnsafe(column)
    }

}