package com.mucheng.text.model.test

import com.mucheng.text.model.standard.TextModel

private const val PATH = "D:\\IDEAProjects\\TextModel\\src\\main\\kotlin\\com\\mucheng\\text\\model\\test\\a.js"

private const val OUTPUT = "D:\\IDEAProjects\\TextModel\\src\\main\\kotlin\\com\\mucheng\\text\\model\\test\\a.output"

private fun main() {
    before()
    body()
    after()
}

private fun before() {
    println("---------- Code Start ----------")
}

private fun after() {
    println("---------- Code End ----------")
}

private fun body() {
    val textModel = TextModel("aa\nbcc\na")
    textModel.getUnsafe(0)
}