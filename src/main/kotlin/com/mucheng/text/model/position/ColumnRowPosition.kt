package com.mucheng.text.model.position

data class ColumnRowPosition(var column: Int, var row: Int, var index: Int = -1) {

    companion object {

        fun createZero(): ColumnRowPosition {
            return ColumnRowPosition(column = 1, row = 0, index = 0)
        }

    }

}