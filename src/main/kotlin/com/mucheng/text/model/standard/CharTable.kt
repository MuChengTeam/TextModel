package com.mucheng.text.model.standard

/**
 * 字符表
 * */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object CharTable {

    /**
     * 空字符串
     * */
    const val EMPTY_STRING: String = ""

    /**
     * 回车
     * */
    const val CR: Char = '\r'

    /**
     * 换行
     * */
    const val LF: Char = '\n'

    /**
     * 回车换行
     * */
    const val CRLF: String = "\r\n"

    /**
     * 空字符
     * */
    const val NULL: Char = '\u0000'

    const val LEFT_PARENTHESIS = '('

    const val RIGHT_PARENTHESIS = ')'

    const val LEFT_BRACKETS = '['

    const val RIGHT_BRACKETS = ']'

    const val LEFT_CURLY_BRACKETS = '{'

    const val RIGHT_CURLY_BRACKETS = '}'

    val BracketCollection = charArrayOf(
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        LEFT_BRACKETS,
        RIGHT_BRACKETS,
        LEFT_CURLY_BRACKETS,
        RIGHT_CURLY_BRACKETS
    )

}