package com.mucheng.text.model.indexer

import com.mucheng.text.model.base.AbstractTextModel
import com.mucheng.text.model.base.IIndexer
import com.mucheng.text.model.event.TextModelEvent
import com.mucheng.text.model.position.ColumnRowPosition
import java.util.Collections
import kotlin.math.abs

@Suppress("LeakingThis", "unused")
open class CachedIndexer(open val textModel: AbstractTextModel) : IIndexer, TextModelEvent {

    companion object {
        const val CACHE_CAPACITY = 100
    }

    private val cache: MutableList<ColumnRowPosition> = ArrayList(CACHE_CAPACITY + 1)

    private var doCache: Boolean = true

    private val zeroPosition: ColumnRowPosition = ColumnRowPosition.createZero()

    private var endPosition: ColumnRowPosition = zeroPosition.copy()

    init {
        textModel.addEvent(this)
    }

    private fun attachEndPos() {
        endPosition.column = textModel.lastColumn
        endPosition.row = textModel.getTextRowSize(endPosition.column)
        endPosition.index = textModel.length
    }

    open fun setCacheUse(isEnabled: Boolean) {
        this.doCache = isEnabled
    }

    open fun isCacheUse(): Boolean {
        return doCache
    }

    override fun columnRowToPosition(column: Int, row: Int): ColumnRowPosition {
        textModel.checkColumnRow(column, row, allowEqualsLength = true)
        val position = findNearestPositionByColumn(column).copy()
        return if (position.column == column) {
            if (position.row == row) {
                return position
            }
            findPositionInColumn(column, row, position)
        } else if (position.column < column) {
            findPositionByColumnRowForward(column, row, position)
        } else {
            findPositionByColumnRowBackward(column, row, position)
        }
    }

    override fun columnRowToIndex(column: Int, row: Int): Int {
        return columnRowToPosition(column, row).index
    }

    override fun indexToPosition(index: Int): ColumnRowPosition {
        textModel.checkIndex(index, allowEqualsLength = true)
        val position = findNearestPositionByIndex(index).copy()
        return if (position.index == index) {
            position
        } else if (position.index < index) {
            findPositionByIndexForward(index, position)
        } else {
            findPositionByIndexBackward(index, position)
        }
    }

    override fun indexToColumn(index: Int): Int {
        return indexToPosition(index).column
    }

    override fun indexToRow(index: Int): Int {
        return indexToPosition(index).row
    }

    /**
     * 此函数返回离 column 处最近的 ColumnRowPosition
     *
     * @param column 目标列
     * @return ColumnRowPosition 最近的位置
     * */
    @Synchronized
    private fun findNearestPositionByColumn(
        column: Int
    ): ColumnRowPosition {
        var targetDistance = column
        // 从缓存池中查找距离最近的 Position
        var targetPos: ColumnRowPosition = zeroPosition
        var targetIndex = 0
        var workIndex = 0
        while (workIndex < cache.size) {
            val pos = cache[workIndex]
            val distance = abs(pos.column - column)
            if (distance < targetDistance) {
                targetDistance = distance
                targetPos = pos
                targetIndex = workIndex
            }
            ++workIndex
        }
        // 如果 targetDistance 离 endPosition 较近, 返回 endPosition
        if (endPosition.column - column < targetDistance) {
            targetPos = endPosition
        }

        // 此位置常用, 交换到最前面
        if (targetPos != zeroPosition && targetPos != endPosition) {
            Collections.swap(cache, 0, targetIndex)
        }
        return targetPos
    }

    /**
     * 此函数将返回 column 列 row 行的位置
     *
     * @param column 目标列
     * @param row 目标行
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionInColumn(
        column: Int,
        row: Int,
        position: ColumnRowPosition
    ): ColumnRowPosition {
        val targetPos = ColumnRowPosition(column, row, position.index - position.row + row)
        push(targetPos)
        return targetPos.copy()
    }

    /**
     * 此函数通过给定的 (column, row) 向前查找位置
     *
     * @param column 目标列
     * @param row 目标行
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByColumnRowForward(
        column: Int,
        row: Int,
        position: ColumnRowPosition
    ): ColumnRowPosition {
        var workColumn: Int = position.column
        var workIndex: Int = position.index

        //Make index to left of line
        workIndex -= position.row

        while (workColumn < column) {
            workIndex += textModel.getTextRowSize(workColumn) + 1
            workColumn++
        }
        val nearestCharPosition = ColumnRowPosition(workColumn, 0, workIndex)
        return findPositionInColumn(column, row, nearestCharPosition)
    }

    /**
     * 此函数通过给定的 (column, row) 向后回溯查找目标位置
     *
     * @param column 目标列
     * @param row 目标行
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByColumnRowBackward(
        column: Int,
        row: Int,
        position: ColumnRowPosition
    ): ColumnRowPosition {
        var workLine: Int = position.column
        var workIndex: Int = position.index

        //Make index to the left of line
        workIndex -= position.row

        while (workLine > column) {
            workIndex -= textModel.getTextRowSize(workLine - 1) + 1
            workLine--
        }
        val nearestCharPosition = ColumnRowPosition(
            workLine, 0, workIndex
        )
        return findPositionInColumn(column, row, nearestCharPosition)
    }

    @Synchronized
    private fun findNearestPositionByIndex(
        index: Int
    ): ColumnRowPosition {
        var targetDistance = index
        // 从缓存池中查找距离最近的 Position
        var targetPos: ColumnRowPosition = zeroPosition
        var targetIndex = 0
        var workIndex = 0
        while (workIndex < cache.size) {
            val pos = cache[workIndex]
            val distance = abs(pos.index - index)
            if (distance < targetDistance) {
                targetDistance = distance
                targetPos = pos
                targetIndex = workIndex
            }
            ++workIndex
        }
        // 如果 targetDistance 离 endPosition 较近, 返回 endPosition
        if (endPosition.index - index < targetDistance) {
            targetPos = endPosition
        }

        // 此位置常用, 交换到最前面
        if (targetPos != zeroPosition && targetPos != endPosition) {
            Collections.swap(cache, 0, targetIndex)
        }
        return targetPos
    }

    /**
     * 此函数通过给定的 index 向前查找位置
     *
     * @param index 目标索引
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByIndexForward(
        index: Int,
        position: ColumnRowPosition
    ): ColumnRowPosition {
        var workColumn = position.column
        var workRow = position.row
        var workIndex = position.index

        val row = textModel.getTextRowSize(workColumn)
        workIndex += row - workRow
        workRow = row

        while (workIndex < index) {
            workColumn++
            workRow = textModel.getTextRowSize(workColumn)
            workIndex += workRow + 1
        }
        if (workIndex > index) {
            workRow -= workIndex - index
        }

        val neededCharPosition = ColumnRowPosition(workColumn, workRow, index)
        push(neededCharPosition)
        return neededCharPosition.copy()
    }

    private fun findPositionByIndexBackward(
        index: Int,
        position: ColumnRowPosition
    ): ColumnRowPosition {
        var workColumn = position.column
        var workRow = position.row
        var workIndex = position.index
        while (workIndex > index) {
            workIndex -= workRow + 1
            workColumn--
            workRow = if (workColumn != -1) {
                textModel.getTextRowSize(workColumn)
            } else {
                return findPositionByIndexForward(index, zeroPosition)
            }
        }
        val nextRow = index - workIndex
        if (nextRow > 0) {
            workColumn++
            workRow = nextRow - 1
        }
        val neededCharPosition = ColumnRowPosition(workColumn, workRow, index)
        push(neededCharPosition)
        return neededCharPosition
    }

    open fun clearCache() {
        cache.clear()
    }

    @Synchronized
    protected open fun push(position: ColumnRowPosition) {
        if (doCache) {
            cache.add(position)
            while (cache.size > CACHE_CAPACITY) {
                cache.removeFirst()
            }
        }
    }

    @Synchronized
    override fun afterInsert(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int, charSequence: CharSequence) {
        // 大于等于进行位置偏移
        for (position in cache) {
            if (position.column == startColumn) {
                if (position.row >= startRow) {
                    position.column += endColumn - startColumn
                    position.row = endRow - position.row + startRow
                    position.index += charSequence.length
                }
            } else if (position.column > startColumn) {
                position.column += endColumn - startColumn
                position.index += charSequence.length
            }
        }
        attachEndPos()
    }

    @Synchronized
    override fun afterDelete(startColumn: Int, startRow: Int, endColumn: Int, endRow: Int, charSequence: CharSequence) {
        // 如果在区间内则进行删除, 否则进行偏移
        val deletedList: MutableList<ColumnRowPosition> = ArrayList()
        for (position in cache) {
            if (position.column == startColumn) {
                if (position.row >= startColumn) {
                    deletedList.add(position)
                }
            } else if (position.column > startColumn) {
                if (position.column <= endColumn) {
                    deletedList.add(position)
                } else {
                    position.column -= endColumn - startColumn
                    position.index -= charSequence.length
                }
            }
        }
        cache.removeAll(deletedList)
        attachEndPos()
    }

}