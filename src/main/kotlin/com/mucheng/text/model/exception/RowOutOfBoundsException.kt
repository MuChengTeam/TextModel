package com.mucheng.text.model.exception

class RowOutOfBoundsException : IndexOutOfBoundsException {

    constructor(row: Int) : super("Row out of range: $row")

    constructor(string: String?) : super(string)

    constructor() : super()

}