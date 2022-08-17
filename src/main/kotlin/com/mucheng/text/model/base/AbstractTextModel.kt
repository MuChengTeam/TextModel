package com.mucheng.text.model.base

import com.mucheng.text.model.event.TextModelEvent
import com.mucheng.text.model.exception.ColumnOutOfBoundsException
import com.mucheng.text.model.exception.IndexOutOfBoundsException
import com.mucheng.text.model.exception.RowOutOfBoundsException
import com.mucheng.text.model.indexer.CachedIndexer
import com.mucheng.text.model.iterator.CharIterator
import com.mucheng.text.model.iterator.TextRowIterator
import com.mucheng.text.model.standard.CharTable
import com.mucheng.text.model.standard.Converter
import com.mucheng.text.model.standard.TextRow
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("LeakingThis", "unused")
abstract class AbstractTextModel(
    capacity: Int,
    private var threadSafe: Boolean
) : ITextModel {

    companion object {
        const val DEFAULT_CAPACITY = 50
    }

    private val value: ArrayList<TextRow>

    private val events: MutableList<TextModelEvent>

    private var _length: Int

    protected var lock: ReadWriteLock?
        private set

    private var indexer: IIndexer

    override val length: Int
        get() {
            return _length
        }

    open val lastIndex: Int
        get() {
            return length - 1
        }

    open val lastColumn: Int
        get() {
            return value.size
        }

    open val capacity: Long
        get() {
            return withLock(false) {
                var capacity: Long = 0
                val len = value.size
                var index = 0
                while (index < len) {
                    capacity += value[index].capacity
                    ++index
                }
                capacity
            }
        }

    init {
        value = if (capacity < DEFAULT_CAPACITY) {
            ArrayList(DEFAULT_CAPACITY)
        } else {
            ArrayList(capacity)
        }
        value.add(TextRow())

        events = ArrayList()
        _length = 0
        lock = if (threadSafe) {
            ReentrantReadWriteLock()
        } else {
            null
        }
        indexer = CachedIndexer(this)
    }

    open fun setThreadSafe(threadSafe: Boolean) {
        this.threadSafe = threadSafe
        lock = if (threadSafe) {
            ReentrantReadWriteLock()
        } else {
            null
        }
    }

    open fun isThreadSafe(): Boolean {
        return threadSafe
    }

    open fun setIndexer(indexer: IIndexer) {
        this.indexer = indexer
    }

    open fun getIndexer(): IIndexer {
        return indexer
    }

    open fun addEvent(event: TextModelEvent) {
        events.add(event)
    }

    open fun removeEvent(event: TextModelEvent) {
        events.remove(event)
    }

    open fun getTextRow(column: Int): TextRow {
        return withLock(false) {
            checkColumn(column)
            getTextRowModelInternal(column)
        }
    }

    private fun getTextRowModelInternal(column: Int): TextRow {
        return value[Converter.columnToIndex(column)]
    }

    open fun getTextRowSize(column: Int): Int {
        return getTextRow(column).length
    }

    override fun get(index: Int): Char {
        return withLock(false) {
            checkIndex(index, allowEqualsLength = false)
            val position = indexer.indexToPosition(index)
            val column = position.column
            val row = position.row
            if (column < value.size) {
                checkColumnRow(column, row, allowEqualsLength = true)
            } else {
                checkColumnRow(column, row, allowEqualsLength = false)
            }
            getInternal(column, row)
        }
    }

    override fun get(column: Int, row: Int): Char {
        return withLock(false) {
            if (column < lastColumn) {
                checkColumnRow(column, row, true)
            } else {
                checkColumnRow(column, row, allowEqualsLength = false)
            }
            getInternal(column, row)
        }
    }

    private fun getInternal(column: Int, row: Int): Char {
        val textRowModelModel = value[Converter.columnToIndex(column)]
        return if (row == textRowModelModel.length) {
            CharTable.LF
        } else {
            textRowModelModel[row]
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return with(false) {
            checkRangeIndex(startIndex, endIndex)
            val startPosition = indexer.indexToPosition(startIndex)
            val endPosition = indexer.indexToPosition(endIndex)
            subSequenceInternal(
                startPosition.column,
                startPosition.row,
                endPosition.column,
                endPosition.row
            )
        }
    }

    override fun subSequence(
        startColumn: Int,
        startRow: Int,
        endColumn: Int,
        endRow: Int
    ): CharSequence {
        return withLock(false) {
            checkColumn(startColumn)
            checkColumn(endColumn)
            subSequenceInternal(startColumn, startRow, endColumn, endRow)
        }
    }

    private fun subSequenceInternal(
        startColumn: Int,
        startRow: Int,
        endColumn: Int,
        endRow: Int
    ): CharSequence {
        val builder = StringBuilder()
        if (startColumn == endColumn) {
            builder.append(getTextRowModelInternal(startColumn).subSequence(startRow, endRow))
        } else {
            val startTextRowModel = getTextRowModelInternal(startColumn)
            val endTextRowModel = getTextRowModelInternal(endColumn)

            builder.append(startTextRowModel.subSequenceAfter(startRow))
            builder.append(CharTable.LF)

            var workColumn = startColumn + 1
            while (workColumn < endColumn) {
                builder.append(getTextRowModelInternal(workColumn))
                builder.append(CharTable.LF)
                ++workColumn
            }

            builder.append(endTextRowModel.subSequenceBefore(endRow))
        }
        return builder
    }

    override fun append(charSequence: CharSequence) {
        withLock(true) {
            val lastColumn = lastColumn
            insertInternal(lastColumn, getTextRowModelInternal(lastColumn).length, charSequence)
        }
    }

    open fun appendUnsafe(charSequence: CharSequence) {
        val lastColumn = lastColumn
        insertInternal(lastColumn, getTextRowModelInternal(lastColumn).length, charSequence)
    }

    override fun insert(column: Int, row: Int, charSequence: CharSequence) {
        withLock(true) {
            checkColumnRow(column, row, allowEqualsLength = true)
            insertInternal(column, row, charSequence)
        }
    }

    private fun insertInternal(column: Int, row: Int, charSequence: CharSequence) {
        val len = charSequence.length
        var textRow: TextRow = getTextRowModelInternal(column)
        var workColumn = column
        var workRow = row
        var workIndex = 0
        while (workIndex < len) {
            val char = charSequence[workIndex]
            if (char != CharTable.LF) {
                textRow.insert(workRow, char)
                ++workRow
            } else {
                textRow = TextRow()
                if (workColumn + 1 <= lastColumn) {
                    value.add(workColumn, textRow)
                } else {
                    value.add(textRow)
                }
                ++workColumn
                workRow = 0
            }
            ++workIndex
        }
        _length += charSequence.length

        for (event in events) {
            event.afterInsert(column, row, workColumn, workRow, charSequence)
        }
    }

    override fun delete(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int) {
        withLock(true) {
            checkColumn(startColumn)
            checkColumn(endColumn)
            deleteInternal(startColumn, startRow, endColumn, endRow)
        }
    }

    private fun deleteInternal(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int) {
        val deleteText = subSequenceInternal(startColumn, startRow, endColumn, endRow)
        if (startColumn == endColumn) {
            val textRowModel = getTextRowModelInternal(startColumn)
            textRowModel.delete(startRow, endRow)
        } else {
            val startTextRowModel = getTextRow(startColumn)
            val endTextRowModel = getTextRow(endColumn)
            val insertedText = endTextRowModel.subSequenceAfter(endRow)

            startTextRowModel.deleteAfter(startRow)
            endTextRowModel.deleteBefore(endRow)

            value.removeAt(Converter.columnToIndex(endColumn))
            val workLine = startColumn + 1
            if (workLine < endColumn) {
                var modCount = 0
                val size = endColumn - workLine
                while (modCount < size) {
                    value.removeAt(Converter.columnToIndex(workLine))
                    ++modCount
                }
            }

            startTextRowModel.append(insertedText)
        }
        _length -= deleteText.length
        for (event in events) {
            event.afterDelete(startColumn, startRow, endColumn, endRow, deleteText)
        }
    }

    override fun deleteCharAt(column: Int, row: Int) {
        withLock(true) {
            if (column < lastColumn) {
                checkColumnRow(column, row, allowEqualsLength = true)
            } else {
                checkColumnRow(column, row, allowEqualsLength = false)
            }
            deleteCharAtInternal(column, row)
        }
    }

    open fun deleteCharAtUnsafe(column: Int, row: Int) {
        if (column < lastColumn) {
            checkColumnRow(column, row, allowEqualsLength = true)
        } else {
            checkColumnRow(column, row, allowEqualsLength = false)
        }
        deleteCharAtInternal(column, row)
    }

    private fun deleteCharAtInternal(column: Int, row: Int) {
        val targetTextRow = value[Converter.columnToIndex(column)]
        val deleteText: CharSequence
        if (row < targetTextRow.length) {
            deleteText = targetTextRow[row].toString()
            targetTextRow.deleteCharAt(row)
        } else {
            val nextTextLineModel = value.removeAt(Converter.columnToIndex(column + 1))
            targetTextRow.append(nextTextLineModel)
            deleteText = CharTable.LF.toString()
        }
        --_length
        for (event in events) {
            event.afterDelete(column, row, column, row + 1, deleteText)
        }
    }

    override fun toString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workColumn = 1
            while (workColumn <= lastColumn) {
                if (workColumn < lastColumn) {
                    builder.append(getTextRowModelInternal(workColumn))
                    builder.append(CharTable.LF)
                } else {
                    builder.append(getTextRowModelInternal(workColumn))
                }
                ++workColumn
            }
            builder.toString()
        }
    }

    open fun toCRString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workColumn = 1
            while (workColumn <= lastColumn) {
                if (workColumn < lastColumn) {
                    builder.append(getTextRowModelInternal(workColumn))
                    builder.append(CharTable.CR)
                } else {
                    builder.append(getTextRowModelInternal(workColumn))
                }
                ++workColumn
            }
            builder.toString()
        }
    }

    open fun toCRLFString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workColumn = 1
            while (workColumn <= lastColumn) {
                if (workColumn < lastColumn) {
                    builder.append(getTextRowModelInternal(workColumn))
                    builder.append(CharTable.CRLF)
                } else {
                    builder.append(getTextRowModelInternal(workColumn))
                }
                ++workColumn
            }
            builder.toString()
        }
    }

    open fun ensureTextRowModelListCapacity(minimumCapacity: Int) {
        val len = value.size
        val targetCapacity: Int = if (minimumCapacity <= len) {
            len + DEFAULT_CAPACITY
        } else {
            minimumCapacity
        }
        value.ensureCapacity(targetCapacity)
    }

    open fun clear() {
        withLock(true) {
            value.clear()
            value.add(TextRow())
            _length = 0
        }
    }

    open fun clearUnsafe() {
        value.clear()
        value.add(TextRow())
        _length = 0
    }

    /**
     * 给 block 块加锁
     *
     * @param writeLock 加写锁, 否则加读锁
     * @param block 代码块
     * @return T 目标类型
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun <T> withLock(writeLock: Boolean, block: () -> T): T {
        val currentLock = lock ?: return block()
        if (writeLock) currentLock.writeLock().lock() else currentLock.readLock().lock()
        return try {
            block()
        } finally {
            if (writeLock) currentLock.writeLock().unlock() else currentLock.readLock().unlock()
        }
    }

    /**
     * 检验目标索引是否越界
     *
     * @param targetIndex 需要检验的索引
     * @throws IndexOutOfBoundsException
     * */
    @Throws(IndexOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkIndex(targetIndex: Int, allowEqualsLength: Boolean) {
        if (targetIndex < 0) {
            throw IndexOutOfBoundsException(targetIndex)
        }
        if (allowEqualsLength) {
            if (targetIndex > length) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        } else {
            if (targetIndex > lastIndex) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        }
    }

    /**
     * 检验目标区间是否越界
     *
     * @param startIndex 需要检验的起始索引
     * @param endIndex 需要检验的结束索引
     * @throws IndexOutOfBoundsException
     * */
    @Throws(IndexOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkRangeIndex(startIndex: Int, endIndex: Int) {
        checkIndex(startIndex, allowEqualsLength = false)
        checkIndex(endIndex, allowEqualsLength = true)
        if (startIndex > endIndex) {
            throw IndexOutOfBoundsException(endIndex - startIndex)
        }
    }

    /**
     * 检验目标列是否越界
     *
     * @param targetColumn 需要检验的列
     * @throws ColumnOutOfBoundsException
     * */
    @Throws(ColumnOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkColumn(targetColumn: Int) {
        if (targetColumn < 1) {
            throw ColumnOutOfBoundsException(targetColumn)
        }
        if (targetColumn > lastColumn) {
            throw ColumnOutOfBoundsException(targetColumn)
        }
    }

    /**
     * 检验目标列行是否越界
     *
     * @param column 需要检验的列
     * @param row 需要检验的行
     * @throws ColumnOutOfBoundsException
     * @throws RowOutOfBoundsException
     * */
    @Throws(RowOutOfBoundsException::class)
    fun checkColumnRow(column: Int, row: Int, allowEqualsLength: Boolean) {
        checkColumn(column)
        if (row < 0) {
            throw RowOutOfBoundsException(row)
        }
        val textRowModelModel = value[Converter.columnToIndex(column)]
        if (allowEqualsLength) {
            if (row > textRowModelModel.length) {
                throw RowOutOfBoundsException(row)
            }
        } else {
            if (row > textRowModelModel.lastIndex) {
                throw RowOutOfBoundsException(row)
            }
        }
    }

    /**
     * 检验目标行列区间是否越界
     *
     * @param startColumn 起始列
     * @param startRow 起始行
     * @param endColumn 结束列
     * @param endRow 结束行
     * @throws ColumnOutOfBoundsException
     * @throws RowOutOfBoundsException
     * */
    @Throws(RowOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkRangeColumnRow(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int) {
        checkColumnRow(startColumn, startRow, allowEqualsLength = true)
        checkColumnRow(endColumn, endRow, allowEqualsLength = true)
        if (startColumn > endColumn) {
            throw ColumnOutOfBoundsException(endColumn - startColumn)
        }
    }

    override fun charIterator(): CharIterator {
        return CharIterator(this)
    }

    override fun textRowIterator(): Iterator<TextRow> {
        return TextRowIterator(this)
    }

    open fun <T> useLock(writeLock: Boolean, block: () -> T): T {
        return withLock(writeLock, block = block)
    }

}