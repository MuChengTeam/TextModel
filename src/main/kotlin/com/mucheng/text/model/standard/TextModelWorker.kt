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
                    textModel.clearUnsafe()
                    var lineText: String?
                    while (bufferedReader.readLine().also { lineText = it } != null) {
                        if (!onReadProgress()) {
                            return@useLock
                        }
                        textModel.appendUnsafe(lineText!!)
                        textModel.appendUnsafe(LF)
                    }
                    val column = textModel.lastColumn - 1
                    if (column > 0) {
                        val row = textModel.getTextRowSize(column)
                        textModel.deleteCharAtUnsafe(column, row)
                    }
                }
            }
        }
    }

    fun copy(textModel: TextModel, onCopyProgress: () -> Boolean = { true }): TextModel {
        val copiedTextModel = TextModel(textModel.lastColumn)
        val iterator = textModel.textRowIterator()
        val LF = CharTable.LF.toString()
        while (iterator.hasNext()) {
            if (!onCopyProgress()) {
                return copiedTextModel
            }
            val textRow = iterator.next().copy()
            copiedTextModel.append(textRow)
            if (iterator.hasNext()) {
                copiedTextModel.append(LF)
            }
        }
        return copiedTextModel
    }

}