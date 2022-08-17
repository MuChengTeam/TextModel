package com.mucheng.text.model.standard

internal object Converter {

    fun columnToIndex(column: Int): Int {
        return column - 1
    }

    fun indexToColumn(index: Int): Int {
        return index + 1
    }

}