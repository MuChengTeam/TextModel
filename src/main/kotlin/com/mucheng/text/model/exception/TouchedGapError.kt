package com.mucheng.text.model.exception

open class TouchedGapError(index: Int) : IllegalAccessError("Index out of range: $index, touched Gap!")