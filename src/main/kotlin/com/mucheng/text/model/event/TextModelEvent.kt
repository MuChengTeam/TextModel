package com.mucheng.text.model.event

interface TextModelEvent {

    fun afterInsert(startLine: Int, startRow: Int, endLine: Int, endRow: Int, charSequence: CharSequence)

    fun afterDelete(startLine: Int, startRow: Int, endLine: Int, endRow: Int, charSequence: CharSequence)

}