package com.mucheng.text.model.exception

class LineOutOfBoundsException : IndexOutOfBoundsException {

    constructor(column: Int) : super("Column out of range: $column")

    constructor(string: String?) : super(string)

    constructor() : super()

}