package com.mucheng.text.model.iterator

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.standard.TextRow

open class TextRowIterator(open val textModel: AbstractTextModel) : Iterator<TextRow> {

    private var line = 0

    override fun hasNext(): Boolean {
        return line + 1 <= textModel.lastLine
    }

    override fun next(): TextRow {
        ++line
        return textModel.getTextRow(line)
    }

}