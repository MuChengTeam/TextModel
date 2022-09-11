package com.mucheng.text.model.position

data class LineRowPosition(var line: Int, var row: Int, var index: Int = -1) {

    companion object {

        fun createZero(): LineRowPosition {
            return LineRowPosition(line = 1, row = 0, index = 0)
        }

    }

}