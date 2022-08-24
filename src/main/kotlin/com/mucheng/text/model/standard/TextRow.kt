package com.mucheng.text.model.standard

import com.mucheng.text.model.exception.TouchedGapError
import com.mucheng.text.model.exception.IndexOutOfBoundsException

@Suppress("unused")
open class TextRow(capacity: Int) : CharSequence {

    companion object {
        const val DEFAULT_CAPACITY = 16
    }

    constructor() : this(DEFAULT_CAPACITY)

    constructor(charSequence: CharSequence) : this(charSequence.length) {
        val len = charSequence.length
        var workIndex = 0
        while (workIndex < len) {
            value[workIndex] = charSequence[workIndex]
            ++workIndex
        }
        _length = len
    }

    private var _length: Int

    private var value: Array<Char?>

    override val length: Int
        get() {
            return _length
        }

    open val capacity: Int
        get() {
            return value.size
        }

    open val lastIndex: Int
        get() {
            return length - 1
        }

    init {
        value = if (capacity < DEFAULT_CAPACITY) {
            arrayOfNulls(DEFAULT_CAPACITY)
        } else {
            arrayOfNulls(capacity)
        }
        _length = 0
    }

    /**
     * 通过 index 获取 char
     *
     * @param index 目标索引
     * @throws IndexOutOfBoundsException 当索引越界时抛出此异常
     * @throws TouchedGapError 一般情况下不应出现此错误
     * */
    override fun get(index: Int): Char {
        checkIndex(index, false)
        return value[index] ?: throw TouchedGapError(index)
    }

    open fun set(index: Int, char: Char): TextRow {
        checkIndex(index, allowEqualsLength = false)
        value[index] = char
        return this
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        checkRangeIndex(startIndex, endIndex)
        if (startIndex == endIndex) {
            return ""
        }

        val len = endIndex - startIndex
        val builder = StringBuilder(len)
        var workIndex = startIndex
        while (workIndex < endIndex) {
            builder.append(value[workIndex])
            ++workIndex
        }
        return builder
    }

    open fun ensureCapacity(minimumCapacity: Int): TextRow {
        val targetCapacity: Int = if (minimumCapacity <= capacity) {
            capacity + DEFAULT_CAPACITY
        } else {
            minimumCapacity
        }
        // 进行扩容
        // 复制内容到此数组中, 底层为 System.arraycopy
        value = value.copyInto(arrayOfNulls(targetCapacity))
        return this
    }

    open fun append(charSequence: CharSequence): TextRow {
        return insert(length, charSequence)
    }

    open fun insert(index: Int, charSequence: CharSequence): TextRow {
        checkIndex(index, allowEqualsLength = true)
        // 插入操作首先要让从 index 开始的元素依次向后移动 length 个长度
        val len = charSequence.length
        val spentCapacity = length + len
        // 如果花费的容量大于当前容量, 则扩容
        if (spentCapacity > capacity) {
            // 默认扩容的大小为需要插入的字符序列的长度 * 2
            ensureCapacity(capacity + len * 2)
        }

        // 此处复制操作是从末尾向后偏移, 然后指针继续向前即可
        var offsetIndex = lastIndex
        while (offsetIndex >= index) {
            value[offsetIndex + len] = value[offsetIndex]
            --offsetIndex
        }

        // 有了空间即可进行插入操作
        var workIndex = 0
        while (workIndex < len) {
            // index + workIndex 就是当前向 value 数组插入的位置
            value[index + workIndex] = charSequence[workIndex]
            ++workIndex
        }
        _length = spentCapacity
        return this
    }

    open fun insert(index: Int, char: Char): TextRow {
        checkIndex(index, allowEqualsLength = true)
        // 插入操作首先要让从 index 开始的元素依次向后移动 length 个长度
        val spentCapacity = length + 1
        // 如果花费的容量大于当前容量, 则扩容
        if (spentCapacity > capacity) {
            // 默认扩容的大小为需要插入的字符序列的长度 * 2
            ensureCapacity(capacity + 1)
        }

        // 此处复制操作是从末尾向后偏移, 然后指针继续向前即可
        var offsetIndex = lastIndex
        while (offsetIndex >= index) {
            value[offsetIndex + 1] = value[offsetIndex]
            --offsetIndex
        }

        value[index] = char
        _length = spentCapacity
        return this
    }

    open fun delete(startIndex: Int, endIndex: Int): TextRow {
        checkRangeIndex(startIndex, endIndex)
        if (startIndex == endIndex) {
            return this
        }
        // 删除操作稍微简单点, 删除完成之后只需要填补空间即可
        val len = endIndex - startIndex
        var workIndex = startIndex
        while (workIndex < endIndex) {
            value[workIndex] = null // 释放内存
            ++workIndex
        }
        // 之后只需要将从 endIndex 起始位置及其后面的 char 进行偏移即可
        var offsetIndex = endIndex
        while (offsetIndex < length) {
            val targetIndex = startIndex + offsetIndex - endIndex
            value[targetIndex] = value[offsetIndex]
            ++offsetIndex
        }
        // 别忘记减去 len
        _length -= len
        return this
    }

    open fun deleteCharAt(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        // 删除操作稍微简单点, 删除完成之后只需要填补空间即可
        value[index] = null

        var offsetIndex = index + 1
        while (offsetIndex < length) {
            val targetIndex = offsetIndex - 1
            value[targetIndex] = value[offsetIndex]
            value[offsetIndex] = null
            ++offsetIndex
        }


        //别忘记减去 len
        --_length
    }

    open fun deleteBefore(index: Int): TextRow {
        checkIndex(index, allowEqualsLength = true)
        delete(0, index)
        return this
    }

    open fun deleteAfter(index: Int): TextRow {
        checkIndex(index, allowEqualsLength = true)
        delete(index, length)
        return this
    }

    open fun subSequenceBefore(index: Int): CharSequence {
        return subSequence(0, index)
    }

    open fun subSequenceAfter(index: Int): CharSequence {
        return subSequence(index, length)
    }

    open fun clear(): TextRow {
        var workIndex = 0
        while (workIndex < length) {
            value[workIndex] = null
            ++workIndex
        }
        _length = 0
        return this
    }

    open fun copy(): TextRow {
        return TextRow(subSequence(0, length))
    }

    override fun toString(): String {
        val builder = StringBuilder(length)
        var workIndex = 0
        while (workIndex < length) {
            builder.append(value[workIndex])
            ++workIndex
        }
        return builder.toString()
    }

    fun unsafeValue(): Array<Char?> {
        return value
    }

    /**
     * 检验目标索引是否越界
     *
     * @param targetIndex 需要检验的索引
     * @throws IndexOutOfBoundsException
     * */
    @Throws(IndexOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkIndex(targetIndex: Int, allowEqualsLength: Boolean) {
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
    private inline fun checkRangeIndex(startIndex: Int, endIndex: Int) {
        checkIndex(startIndex, allowEqualsLength = true)
        checkIndex(endIndex, allowEqualsLength = true) // 结束索引不包含, 因此允许等于 length
        if (startIndex > endIndex) {
            throw IndexOutOfBoundsException(endIndex - startIndex)
        }
    }

}