package com.example.darkgo.core

/**
 * Result of validating a 5x5 Bingo card.
 */
sealed class CardValidationResult {
    object Valid : CardValidationResult()
    data class InvalidSize(val expected: Int = 25, val actual: Int) : CardValidationResult()
    data class OutOfRange(val numbers: List<Int>) : CardValidationResult()
    data class DuplicateNumbers(val duplicates: Set<Int>) : CardValidationResult()
    data class MissingNumbers(val missing: Set<Int>) : CardValidationResult()
}

enum class LineType {
    HORIZONTAL_ROW,
    VERTICAL_COLUMN,
    MAIN_DIAGONAL,    // Top-left to bottom-right
    ANTI_DIAGONAL     // Top-right to bottom-left
}

data class CompletedLine(
    val type: LineType,
    val index: Int, // 0..4 for rows/columns, 0 for diagonals
    val label: String,
    val cellIndices: List<Int>
)

/**
 * Pure, authoritative Bingo engine for Dark Go.
 *
 * Straight-line Rules:
 * - 5x5 grid containing numbers 1..25 exactly once.
 * - Straight lines include:
 *   1. 5 Horizontal Rows (Row 0..4)
 *   2. 5 Vertical Columns (Col 0..4)
 *   3. 2 Diagonals (Main: 0-6-12-18-24, Anti: 4-8-12-16-20)
 *   Total = 12 possible straight lines.
 * - Completing 5 straight lines in any combination triggers BINGO!
 */
class BingoEngine {

    companion object {
        const val GRID_SIZE = 5
        const val TOTAL_CELLS = 25
        val VALID_NUMBER_RANGE = 1..25
        const val REQUIRED_LINES_FOR_BINGO = 5

        // All 12 straight lines on a 5x5 board mapped to cell indices (0..24)
        val ALL_STRAIGHT_LINES: List<Pair<LineType, List<Int>>> = buildList {
            // 5 Horizontal rows
            for (r in 0 until GRID_SIZE) {
                add(LineType.HORIZONTAL_ROW to (0 until GRID_SIZE).map { c -> r * GRID_SIZE + c })
            }
            // 5 Vertical columns
            for (c in 0 until GRID_SIZE) {
                add(LineType.VERTICAL_COLUMN to (0 until GRID_SIZE).map { r -> r * GRID_SIZE + c })
            }
            // Main diagonal (top-left to bottom-right)
            add(LineType.MAIN_DIAGONAL to (0 until GRID_SIZE).map { i -> i * GRID_SIZE + i })
            // Anti-diagonal (top-right to bottom-left)
            add(LineType.ANTI_DIAGONAL to (0 until GRID_SIZE).map { i -> i * GRID_SIZE + (GRID_SIZE - 1 - i) })
        }
    }

    /**
     * Validates that the card has exactly 25 distinct numbers ranging strictly from 1 to 25.
     */
    fun validateCard(numbers: List<Int>): CardValidationResult {
        if (numbers.size != TOTAL_CELLS) {
            return CardValidationResult.InvalidSize(TOTAL_CELLS, numbers.size)
        }

        val outOfRange = numbers.filter { it !in VALID_NUMBER_RANGE }
        if (outOfRange.isNotEmpty()) {
            return CardValidationResult.OutOfRange(outOfRange)
        }

        val seen = mutableSetOf<Int>()
        val duplicates = mutableSetOf<Int>()
        for (num in numbers) {
            if (!seen.add(num)) {
                duplicates.add(num)
            }
        }
        if (duplicates.isNotEmpty()) {
            return CardValidationResult.DuplicateNumbers(duplicates)
        }

        val expected = (1..TOTAL_CELLS).toSet()
        val missing = expected - seen
        if (missing.isNotEmpty()) {
            return CardValidationResult.MissingNumbers(missing)
        }

        return CardValidationResult.Valid
    }

    fun isNumberCalled(number: Int, calledNumbers: Set<Int>): Boolean {
        return calledNumbers.contains(number)
    }

    /**
     * Returns all completed straight lines (horizontal, vertical, diagonals).
     */
    fun getCompletedLines(card: List<Int>, calledNumbers: Set<Int>): List<CompletedLine> {
        if (card.size != TOTAL_CELLS) return emptyList()

        val completed = mutableListOf<CompletedLine>()
        ALL_STRAIGHT_LINES.forEachIndexed { lineIdx, (type, indices) ->
            val isComplete = indices.all { cellIdx ->
                calledNumbers.contains(card[cellIdx])
            }
            if (isComplete) {
                val (index, label) = when (type) {
                    LineType.HORIZONTAL_ROW -> (lineIdx) to "Row ${lineIdx + 1}"
                    LineType.VERTICAL_COLUMN -> (lineIdx - 5) to "Col ${lineIdx - 4}"
                    LineType.MAIN_DIAGONAL -> 0 to "Diagonal ↘"
                    LineType.ANTI_DIAGONAL -> 1 to "Diagonal ↙"
                }
                completed.add(CompletedLine(type, index, label, indices))
            }
        }
        return completed
    }

    /**
     * Returns set of all cell indices (0..24) that belong to at least one completed straight line.
     * Used for board visualization to highlight completed lines.
     */
    fun getCompletedCellIndices(card: List<Int>, calledNumbers: Set<Int>): Set<Int> {
        return getCompletedLines(card, calledNumbers)
            .flatMap { it.cellIndices }
            .toSet()
    }

    /**
     * Returns the count of completed straight lines (horizontal, vertical, diagonal).
     */
    fun completedLines(card: List<Int>, calledNumbers: Set<Int>): Int {
        return getCompletedLines(card, calledNumbers).size
    }

    /**
     * Alias for completedLines for backward compatibility.
     */
    fun completedRows(card: List<Int>, calledNumbers: Set<Int>): Int {
        return completedLines(card, calledNumbers)
    }

    /**
     * Returns indices of completed horizontal rows (0..4).
     */
    fun getCompletedRowIndices(card: List<Int>, calledNumbers: Set<Int>): List<Int> {
        if (card.size != TOTAL_CELLS) return emptyList()
        return (0 until GRID_SIZE).filter { row ->
            val start = row * GRID_SIZE
            (0 until GRID_SIZE).all { col -> calledNumbers.contains(card[start + col]) }
        }
    }

    /**
     * Returns indices of completed vertical columns (0..4).
     */
    fun getCompletedColIndices(card: List<Int>, calledNumbers: Set<Int>): List<Int> {
        if (card.size != TOTAL_CELLS) return emptyList()
        return (0 until GRID_SIZE).filter { col ->
            (0 until GRID_SIZE).all { row -> calledNumbers.contains(card[row * GRID_SIZE + col]) }
        }
    }

    /**
     * Evaluates whether the card has achieved Bingo (at least 5 completed straight lines).
     */
    fun hasBingo(card: List<Int>, calledNumbers: Set<Int>): Boolean {
        return completedLines(card, calledNumbers) >= REQUIRED_LINES_FOR_BINGO
    }

    /**
     * Generates a random valid 5x5 card with numbers 1..25 shuffled.
     */
    fun generateRandomCard(): List<Int> {
        return (1..TOTAL_CELLS).shuffled()
    }
}
