package com.mucheng.text.model.base

import com.mucheng.text.model.position.LineRowPosition

interface IIndexer {

    fun lineRowToPosition(line: Int, row: Int): LineRowPosition

    fun lineRowToIndex(line: Int, row: Int): Int

    fun indexToPosition(index: Int): LineRowPosition

    fun indexToColumn(index: Int): Int

    fun indexToRow(index: Int): Int

}