package com.mucheng.text.model.standard

import java.io.Reader
import java.io.Writer

@Suppress("LocalVariableName")
object TextModelWorker {

    fun save(textModel: TextModel, writer: Writer, onSaveProgress: () -> Boolean = { true }) {
        val bufferedWriter = writer.buffered()
        writer.use {
            bufferedWriter.use {
                textModel.useLock(false) {
                    val iterator = textModel.textRowIterator()
                    while (iterator.hasNext()) {
                        if (!onSaveProgress()) {
                            return@useLock
                        }
                        val textRow = iterator.next()
                        bufferedWriter.write(textRow.toString())
                        bufferedWriter.flush()
                        if (iterator.hasNext()) {
                            bufferedWriter.newLine()
                            bufferedWriter.flush()
                        }
                    }
                }
            }
        }
    }

    fun read(textModel: TextModel, reader: Reader, onReadProgress: () -> Boolean = { true }) {
        val LF = CharTable.LF.toString()
        val bufferedReader = reader.buffered()
        reader.use {
            bufferedReader.use {
                textModel.useLock(true) {
                    textModel.clear()
                    var lineText: String?
                    while (bufferedReader.readLine().also { lineText = it } != null) {
                        if (!onReadProgress()) {
                            return@useLock
                        }
                        textModel.append(lineText!!)
                        textModel.append(LF)
                    }
                    val column = textModel.lastColumn - 1
                    val row = textModel.getTextRowSize(column)
                    textModel.deleteCharAt(column, row)
                }
            }
        }
    }

}