package com.mucheng.text.model.base

import com.mucheng.text.model.position.ColumnRowPosition

interface IIndexer {

    fun columnRowToPosition(column: Int, row: Int): ColumnRowPosition

    fun columnRowToIndex(column: Int, row: Int): Int

    fun indexToPosition(index: Int): ColumnRowPosition

    fun indexToColumn(index: Int): Int

    fun indexToRow(index: Int): Int

}