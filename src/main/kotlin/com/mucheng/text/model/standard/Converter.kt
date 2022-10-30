package com.mucheng.text.model.standard

internal object Converter {

    fun lineToIndex(line: Int): Int {
        return line - 1
    }

    fun indexToLine(index: Int): Int {
        return index + 1
    }

}