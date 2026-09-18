package com.example.darkgo.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BingoEngineTest {

    private lateinit var bingoEngine: BingoEngine

    @Before
    fun setUp() {
        bingoEngine = BingoEngine()
    }

    @Test
    fun testGenerateRandomCard_isValid() {
        val card = bingoEngine.generateRandomCard()
        assertEquals(25, card.size)
        assertEquals((1..25).toSet(), card.toSet())
        assertTrue(bingoEngine.validateCard(card) is CardValidationResult.Valid)
    }

    @Test
    fun testValidateCard_invalidSize() {
        val incompleteCard = (1..20).toList()
        val result = bingoEngine.validateCard(incompleteCard)
        assertTrue(result is CardValidationResult.InvalidSize)
    }

    @Test
    fun testValidateCard_duplicates() {
        // 25 numbers but with a duplicate and missing number
        val list = (1..24).toMutableList().apply { add(1) } // 1 is duplicated, 25 is missing
        val result = bingoEngine.validateCard(list)
        assertTrue(result is CardValidationResult.DuplicateNumbers)
    }

    @Test
    fun testValidateCard_outOfRange() {
        val list = (1..24).toMutableList().apply { add(26) }
        val result = bingoEngine.validateCard(list)
        assertTrue(result is CardValidationResult.OutOfRange)
    }

    @Test
    fun testHorizontalRowCompletion() {
        // Standard ordered card:
        // Row 0: 1, 2, 3, 4, 5
        // Row 1: 6, 7, 8, 9, 10
        // Row 2: 11, 12, 13, 14, 15
        // Row 3: 16, 17, 18, 19, 20
        // Row 4: 21, 22, 23, 24, 25
        val card = (1..25).toList()

        // Call 4 numbers of row 0 -> should not complete row
        val partialCalled = setOf(1, 2, 3, 4)
        assertEquals(0, bingoEngine.completedLines(card, partialCalled))
        assertFalse(bingoEngine.hasBingo(card, partialCalled))

        // Call 5th number -> Row 0 complete (1 straight line)
        val row0Called = setOf(1, 2, 3, 4, 5)
        assertEquals(1, bingoEngine.completedLines(card, row0Called))
        assertEquals(listOf(0), bingoEngine.getCompletedRowIndices(card, row0Called))
        assertFalse(bingoEngine.hasBingo(card, row0Called))

        // Call row 1 as well -> 2 straight lines complete
        val row0And1 = row0Called + setOf(6, 7, 8, 9, 10)
        assertEquals(2, bingoEngine.completedLines(card, row0And1))
        assertEquals(listOf(0, 1), bingoEngine.getCompletedRowIndices(card, row0And1))
    }

    @Test
    fun testVerticalColumnsCountAsStraightLines() {
        val card = (1..25).toList()
        // Column 0: 1, 6, 11, 16, 21
        val col0 = setOf(1, 6, 11, 16, 21)
        val lines = bingoEngine.getCompletedLines(card, col0)
        assertEquals(1, lines.size)
        assertEquals(LineType.VERTICAL_COLUMN, lines[0].type)
        assertEquals(0, lines[0].index)
    }

    @Test
    fun testDiagonalsCountAsStraightLines() {
        val card = (1..25).toList()
        // Main diagonal: 1, 7, 13, 19, 25
        val mainDiag = setOf(1, 7, 13, 19, 25)
        val lines = bingoEngine.getCompletedLines(card, mainDiag)
        assertEquals(1, lines.size)
        assertEquals(LineType.MAIN_DIAGONAL, lines[0].type)

        // Anti diagonal: 5, 9, 13, 17, 21
        val antiDiag = setOf(5, 9, 13, 17, 21)
        val linesAnti = bingoEngine.getCompletedLines(card, antiDiag)
        assertEquals(1, linesAnti.size)
        assertEquals(LineType.ANTI_DIAGONAL, linesAnti[0].type)
    }

    @Test
    fun testAnyFiveStraightLinesTriggersBingo() {
        val card = (1..25).toList()
        // Combination of 5 straight lines:
        // Col 0: 1, 6, 11, 16, 21
        // Col 1: 2, 7, 12, 17, 22
        // Col 2: 3, 8, 13, 18, 23
        // Col 3: 4, 9, 14, 19, 24
        // Col 4: 5, 10, 15, 20, 25
        // All 5 columns complete = 5 lines -> BINGO!
        val allColumns = (1..25).toSet()
        assertTrue(bingoEngine.hasBingo(card, allColumns))
        assertEquals(12, bingoEngine.completedLines(card, allColumns)) // 5 rows + 5 cols + 2 diags = 12 lines
    }

    @Test
    fun testCombinedRowsColsDiagsForBingo() {
        val card = (1..25).toList()
        // Complete Row 0 (1, 2, 3, 4, 5)
        // Complete Row 4 (21, 22, 23, 24, 25)
        // Complete Col 0 (1, 6, 11, 16, 21)
        // Complete Col 4 (5, 10, 15, 20, 25)
        // Complete Main Diagonal (1, 7, 13, 19, 25)
        // Total unique numbers needed: 1,2,3,4,5, 6,11,16,21, 22,23,24,25, 10,15,20, 7,13,19
        val calledNumbers = setOf(
            1, 2, 3, 4, 5,
            6, 11, 16, 21,
            22, 23, 24, 25,
            10, 15, 20,
            7, 13, 19
        )
        val completedLines = bingoEngine.completedLines(card, calledNumbers)
        assertTrue(completedLines >= 5)
        assertTrue(bingoEngine.hasBingo(card, calledNumbers))
    }
}
