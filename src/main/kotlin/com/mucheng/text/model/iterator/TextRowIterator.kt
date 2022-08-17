package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.standard.TextRow

open class TextRowIterator(private val textModel: AbstractTextModel) : Iterator<TextRow> {

    private var column = 0

    override fun hasNext(): Boolean {
        return column + 1 <= textModel.lastColumn
    }

    override fun next(): TextRow {
        ++column
        return textModel.getTextRow(column)
    }

}