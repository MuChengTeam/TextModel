package com.mucheng.text.model.event

interface TextModelEvent {

    fun afterInsert(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int, charSequence: CharSequence)

    fun afterDelete(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int, charSequence: CharSequence)

}