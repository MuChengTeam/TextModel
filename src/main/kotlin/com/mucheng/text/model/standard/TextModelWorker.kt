package com.mucheng.text.model.standard

import java.io.Reader
import java.io.Writer

/**
 * 此类封装了 TextModel 的常用功能
 * */
@Suppress("LocalVariableName")
object TextModelWorker {

    /**
     * 将 TextModel 的内容写到 Writer 中
     *
     * @param textModel 文本模型
     * @param writer 目标的 Writer 对象
     * @param onSaveProgress 当每次行存储时都会调用这个 block, 返回 false 即可中断操作
     * */
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

    /**
     * 从 reader 读取内容到 textModel 中
     *
     * @param textModel 文本模型
     * @param reader 目标的 Reader 对象
     * @param onReadProgress 当每次行读取时都会调用这个 block, 返回 false 即可中断操作
     * */
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

    /**
     * 拷贝一个新的 TextModel 对象
     *
     * @param textModel 文本模型
     * @param onCopyProgress 当每次行拷贝时都会调用这个 block, 返回 false 即可中断操作
     * @return 拷贝后的 TextModel 对象
     * */
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