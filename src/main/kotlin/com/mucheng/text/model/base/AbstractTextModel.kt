package com.mucheng.text.model.base

import com.mucheng.text.model.event.TextModelEvent
import com.mucheng.text.model.exception.IndexOutOfBoundsException
import com.mucheng.text.model.exception.LineOutOfBoundsException
import com.mucheng.text.model.exception.RowOutOfBoundsException
import com.mucheng.text.model.indexer.CachedIndexer
import com.mucheng.text.model.iterator.CharIterator
import com.mucheng.text.model.iterator.CharIteratorUnsafe
import com.mucheng.text.model.iterator.TextRowIterator
import com.mucheng.text.model.iterator.TextRowIteratorUnsafe
import com.mucheng.text.model.mark.UnsafeApi
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

    open val lastLine: Int
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
        value.add(createTextRow())

        events = ArrayList()
        _length = 0
        lock = if (threadSafe) {
            ReentrantReadWriteLock()
        } else {
            null
        }
        indexer = CachedIndexer(this)
    }

    protected open fun createTextRow(): TextRow {
        return TextRow()
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

    open fun getTextRow(line: Int): TextRow {
        return withLock(false) {
            checkLine(line)
            getTextRowModelInternal(line)
        }
    }

    @UnsafeApi
    open fun getTextRowUnsafe(line: Int): TextRow {
        checkLine(line)
        return getTextRowModelInternal(line)
    }

    private fun getTextRowModelInternal(line: Int): TextRow {
        return value[Converter.lineToIndex(line)]
    }

    open fun getTextRowSize(line: Int): Int {
        return getTextRow(line).length
    }

    @UnsafeApi
    open fun getTextRowSizeUnsafe(line: Int): Int {
        return getTextRowUnsafe(line).length
    }

    override fun get(index: Int): Char {
        return withLock(false) {
            checkIndex(index, allowEqualsLength = false)
            val position = indexer.indexToPosition(index)
            val line = position.line
            val row = position.row
            if (line < value.size) {
                checkLineRow(line, row, allowEqualsLength = true)
            } else {
                checkLineRow(line, row, allowEqualsLength = false)
            }
            getInternal(line, row)
        }
    }

    @UnsafeApi
    open fun getUnsafe(index: Int): Char {
        checkIndex(index, allowEqualsLength = false)
        val position = indexer.indexToPosition(index)
        val line = position.line
        val row = position.row
        if (line < value.size) {
            checkLineRow(line, row, allowEqualsLength = true)
        } else {
            checkLineRow(line, row, allowEqualsLength = false)
        }
        return getInternal(line, row)
    }

    override fun get(line: Int, row: Int): Char {
        return withLock(false) {
            if (line < lastLine) {
                checkLineRow(line, row, true)
            } else {
                checkLineRow(line, row, allowEqualsLength = false)
            }
            getInternal(line, row)
        }
    }

    @UnsafeApi
    open fun getUnsafe(line: Int, row: Int): Char {
        if (line < lastLine) {
            checkLineRow(line, row, true)
        } else {
            checkLineRow(line, row, allowEqualsLength = false)
        }
        return getInternal(line, row)
    }

    private fun getInternal(line: Int, row: Int): Char {
        val textRowModelModel = value[Converter.lineToIndex(line)]
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
                startPosition.line,
                startPosition.row,
                endPosition.line,
                endPosition.row
            )
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(startIndex: Int, endIndex: Int): CharSequence {
        checkRangeIndex(startIndex, endIndex)
        val startPosition = indexer.indexToPosition(startIndex)
        val endPosition = indexer.indexToPosition(endIndex)
        return subSequenceInternal(
            startPosition.line,
            startPosition.row,
            endPosition.line,
            endPosition.row
        )
    }

    override fun subSequence(
        startLine: Int,
        startRow: Int,
        endLine: Int,
        endRow: Int
    ): CharSequence {
        return withLock(false) {
            checkLine(startLine)
            checkLine(endLine)
            subSequenceInternal(startLine, startRow, endLine, endRow)
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(
        startLine: Int,
        startRow: Int,
        endLine: Int,
        endRow: Int
    ): CharSequence {
        checkLine(startLine)
        checkLine(endLine)
        return subSequenceInternal(startLine, startRow, endLine, endRow)
    }

    private fun subSequenceInternal(
        startLine: Int,
        startRow: Int,
        endLine: Int,
        endRow: Int
    ): CharSequence {
        val builder = StringBuilder()
        if (startLine == endLine) {
            builder.append(getTextRowModelInternal(startLine).subSequence(startRow, endRow))
        } else {
            val startTextRowModel = getTextRowModelInternal(startLine)
            val endTextRowModel = getTextRowModelInternal(endLine)

            builder.append(startTextRowModel.subSequenceAfter(startRow))
            builder.append(CharTable.CONSTANT_NEW_LINE)

            var workLine = startLine + 1
            while (workLine < endLine) {
                builder.append(getTextRowModelInternal(workLine))
                builder.append(CharTable.CONSTANT_NEW_LINE)
                ++workLine
            }

            builder.append(endTextRowModel.subSequenceBefore(endRow))
        }
        return builder
    }

    override fun append(charSequence: CharSequence) {
        withLock(true) {
            val lastLine = lastLine
            insertInternal(lastLine, getTextRowModelInternal(lastLine).length, charSequence)
        }
    }

    @UnsafeApi
    open fun appendUnsafe(charSequence: CharSequence) {
        val lastLine = lastLine
        insertInternal(lastLine, getTextRowModelInternal(lastLine).length, charSequence)
    }

    override fun insert(index: Int, charSequence: CharSequence) {
        withLock(true) {
            checkIndex(index, allowEqualsLength = true)
            val position = indexer.indexToPosition(index)
            insertInternal(position.line, position.row, charSequence)
        }
    }

    @UnsafeApi
    open fun insertUnsafe(index: Int, charSequence: CharSequence) {
        checkIndex(index, allowEqualsLength = true)
        val position = indexer.indexToPosition(index)
        insertInternal(position.line, position.row, charSequence)
    }

    override fun insert(line: Int, row: Int, charSequence: CharSequence) {
        withLock(true) {
            checkLineRow(line, row, allowEqualsLength = true)
            insertInternal(line, row, charSequence)
        }
    }

    @UnsafeApi
    open fun insertUnsafe(line: Int, row: Int, charSequence: CharSequence) {
        checkLineRow(line, row, allowEqualsLength = true)
        insertInternal(line, row, charSequence)
    }

    private fun insertInternal(line: Int, row: Int, charSequence: CharSequence) {
        val len = charSequence.length
        var textRow: TextRow = getTextRowModelInternal(line)
        var workLine = line
        var workRow = row
        var workIndex = 0
        while (workIndex < len) {
            val char = charSequence[workIndex]
            if (char == CharTable.CR) {
                if (workIndex + 1 < len && charSequence[workIndex + 1] == CharTable.LF) {
                    val nextTextRow = createTextRow()
                    nextTextRow.append(textRow.subSequenceAfter(workRow))
                    textRow.deleteAfter(workRow)

                    // thisIndex = Converter.lineToIndex(workLine + 1)
                    value.add(workLine, nextTextRow)
                    textRow = nextTextRow
                    ++workLine
                    workRow = 0

                    // 因为 '\r' 和 '\n' 被算做了一个字符, 所以 --_length
                    --_length
                    // 因为提前向下一个 char 扫描了, 别忘记 ++workIndex
                    ++workIndex
                } else {
                    textRow.insert(workRow, char)
                    ++workRow
                }
            } else if (char == CharTable.LF) {
                val nextTextRow = createTextRow()
                nextTextRow.append(textRow.subSequenceAfter(workRow))
                textRow.deleteAfter(workRow)

                // thisIndex = Converter.lineToIndex(workLine + 1)
                value.add(workLine, nextTextRow)
                textRow = nextTextRow
                ++workLine
                workRow = 0
            } else {
                textRow.insert(workRow, char)
                ++workRow
            }

            ++workIndex
        }
        _length += charSequence.length

        for (event in events) {
            event.afterInsert(line, row, workLine, workRow, charSequence)
        }
    }

    override fun delete(startIndex: Int, endIndex: Int) {
        withLock(true) {
            checkRangeIndex(startIndex, endIndex)
            val startPosition = indexer.indexToPosition(startIndex)
            val endPosition = indexer.indexToPosition(endIndex)
            deleteInternal(
                startPosition.line,
                startPosition.row,
                endPosition.line,
                endPosition.row
            )
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startIndex: Int, endIndex: Int) {
        checkRangeIndex(startIndex, endIndex)
        val startPosition = indexer.indexToPosition(startIndex)
        val endPosition = indexer.indexToPosition(endIndex)
        deleteInternal(
            startPosition.line,
            startPosition.row,
            endPosition.line,
            endPosition.row
        )
    }

    override fun delete(startLine: Int, startRow: Int, endLine: Int, endRow: Int) {
        withLock(true) {
            checkRangeLineRow(startLine, startRow, endLine, endRow)
            deleteInternal(startLine, startRow, endLine, endRow)
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startLine: Int, startRow: Int, endLine: Int, endRow: Int) {
        checkRangeLineRow(startLine, startRow, endLine, endRow)
        return deleteInternal(startLine, startRow, endLine, endRow)
    }

    private fun deleteInternal(startLine: Int, startRow: Int, endLine: Int, endRow: Int) {
        val deleteText = subSequenceInternal(startLine, startRow, endLine, endRow)
        if (startLine == endLine) {
            val textRowModel = getTextRowModelInternal(startLine)
            textRowModel.delete(startRow, endRow)
        } else {
            val startTextRowModel = getTextRow(startLine)
            val endTextRowModel = getTextRow(endLine)
            val insertedText = endTextRowModel.subSequenceAfter(endRow)

            startTextRowModel.deleteAfter(startRow)
            endTextRowModel.deleteBefore(endRow)

            value.removeAt(Converter.lineToIndex(endLine))
            val workLine = startLine + 1
            if (workLine < endLine) {
                var modCount = 0
                val size = endLine - workLine
                while (modCount < size) {
                    value.removeAt(Converter.lineToIndex(workLine))
                    ++modCount
                }
            }

            startTextRowModel.append(insertedText)
        }
        _length -= deleteText.length
        for (event in events) {
            event.afterDelete(startLine, startRow, endLine, endRow, deleteText)
        }
    }

    override fun deleteCharAt(index: Int) {
        withLock(true) {
            checkIndex(index, allowEqualsLength = false)
            val position = indexer.indexToPosition(index)
            deleteCharAtInternal(position.line, position.row)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        val position = indexer.indexToPosition(index)
        deleteCharAtInternal(position.line, position.row)
    }

    override fun deleteCharAt(line: Int, row: Int) {
        withLock(true) {
            if (line < lastLine) {
                checkLineRow(line, row, allowEqualsLength = true)
            } else {
                checkLineRow(line, row, allowEqualsLength = false)
            }
            deleteCharAtInternal(line, row)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(line: Int, row: Int) {
        if (line < lastLine) {
            checkLineRow(line, row, allowEqualsLength = true)
        } else {
            checkLineRow(line, row, allowEqualsLength = false)
        }
        deleteCharAtInternal(line, row)
    }

    private fun deleteCharAtInternal(line: Int, row: Int) {
        val targetTextRow = value[Converter.lineToIndex(line)]
        val deleteText: CharSequence
        if (row < targetTextRow.length) {
            deleteText = targetTextRow[row].toString()
            targetTextRow.deleteCharAt(row)
        } else {
            val nextTextLineModel = value.removeAt(Converter.lineToIndex(line + 1))
            targetTextRow.append(nextTextLineModel)
            deleteText = CharTable.CONSTANT_NEW_LINE
        }
        --_length
        for (event in events) {
            event.afterDelete(line, row, line, row + 1, deleteText)
        }
    }



    override fun toString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowModelInternal(workLine))
                    builder.append(CharTable.CONSTANT_NEW_LINE)
                } else {
                    builder.append(getTextRowModelInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCRString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowModelInternal(workLine))
                    builder.append(CharTable.CR)
                } else {
                    builder.append(getTextRowModelInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toLFString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowModelInternal(workLine))
                    builder.append(CharTable.LF)
                } else {
                    builder.append(getTextRowModelInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCRLFString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowModelInternal(workLine))
                    builder.append(CharTable.CRLF)
                } else {
                    builder.append(getTextRowModelInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCString() {
        return withLock(false) {
            val builder = StringBuilder(length + 1)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowModelInternal(workLine))
                    builder.append(CharTable.CONSTANT_NEW_LINE)
                } else {
                    builder.append(getTextRowModelInternal(workLine))
                }
                ++workLine
            }
            builder.append(CharTable.NULL)
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
            value.add(createTextRow())
            _length = 0
        }
    }

    @UnsafeApi
    open fun clearUnsafe() {
        value.clear()
        value.add(createTextRow())
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
        checkIndex(startIndex, allowEqualsLength = true)
        checkIndex(endIndex, allowEqualsLength = true)
        if (startIndex > endIndex) {
            throw IndexOutOfBoundsException(endIndex - startIndex)
        }
    }

    /**
     * 检验目标列是否越界
     *
     * @param targetLine 需要检验的列
     * @throws LineOutOfBoundsException
     * */
    @Throws(LineOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkLine(targetLine: Int) {
        if (targetLine < 1) {
            throw LineOutOfBoundsException(targetLine)
        }
        if (targetLine > lastLine) {
            throw LineOutOfBoundsException(targetLine)
        }
    }

    /**
     * 检验目标列行是否越界
     *
     * @param line 需要检验的列
     * @param row 需要检验的行
     * @throws LineOutOfBoundsException
     * @throws RowOutOfBoundsException
     * */
    @Throws(RowOutOfBoundsException::class)
    fun checkLineRow(line: Int, row: Int, allowEqualsLength: Boolean) {
        checkLine(line)
        if (row < 0) {
            throw RowOutOfBoundsException(row)
        }
        val textRowModelModel = value[Converter.lineToIndex(line)]
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
     * @param startLine 起始列
     * @param startRow 起始行
     * @param endLine 结束列
     * @param endRow 结束行
     * @throws LineOutOfBoundsException
     * @throws RowOutOfBoundsException
     * */
    @Throws(RowOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkRangeLineRow(startLine: Int, startRow: Int, endLine: Int, endRow: Int) {
        checkLineRow(startLine, startRow, allowEqualsLength = true)
        checkLineRow(endLine, endRow, allowEqualsLength = true)
        if (startLine > endLine) {
            throw LineOutOfBoundsException(endLine - startLine)
        }
    }

    override fun charIterator(): CharIterator {
        return CharIterator(this)
    }

    @UnsafeApi
    open fun charIteratorUnsafe(): CharIterator {
        return CharIteratorUnsafe(this)
    }

    override fun textRowIterator(): Iterator<TextRow> {
        return TextRowIterator(this)
    }

    @UnsafeApi
    open fun textRowIteratorUnsafe(): Iterator<TextRow> {
        return TextRowIteratorUnsafe(this)
    }

    open fun <T> useLock(writeLock: Boolean, block: () -> T): T {
        return withLock(writeLock, block = block)
    }

}