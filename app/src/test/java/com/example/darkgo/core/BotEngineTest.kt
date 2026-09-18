package com.example.darkgo.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BotEngineTest {

    private lateinit var bingoEngine: BingoEngine
    private lateinit var botEngine: BotEngine

    @Before
    fun setUp() {
        bingoEngine = BingoEngine()
        botEngine = BotEngine(bingoEngine)
    }

    @Test
    fun testCreateBots() {
        val bots = botEngine.createBots(3)
        assertEquals(3, bots.size)
        bots.forEach { bot ->
            assertEquals(25, bot.card.size)
            assertTrue(bingoEngine.validateCard(bot.card) is CardValidationResult.Valid)
        }
    }

    @Test
    fun testChooseNumber_neverPicksAlreadyCalled() {
        val card = (1..25).toList()
        val called = mutableSetOf<Int>()

        // Call 20 numbers
        for (i in 1..20) {
            called.add(i)
        }

        val chosen = botEngine.chooseNumber(card, called)
        assertNotNull(chosen)
        assertFalse(called.contains(chosen!!))
        assertTrue(chosen in 21..25)
    }

    @Test
    fun testChooseNumber_prioritizesRowNearCompletion() {
        // Row 0: 1, 2, 3, 4, 5
        // Row 1: 6, 7, 8, 9, 10
        val card = (1..25).toList()

        // 4 numbers in Row 0 called: 1, 2, 3, 4
        // Row 1 has only 1 called: 6
        val called = setOf(1, 2, 3, 4, 6)

        val chosen = botEngine.chooseNumber(card, called)
        // Bot should strategically pick 5 to complete Row 0!
        assertEquals(5, chosen)
    }

    @Test
    fun testChooseNumber_prioritizesColumnNearCompletion() {
        val card = (1..25).toList()
        // Column 0: 1, 6, 11, 16, 21
        // 4 numbers in Col 0 called: 1, 6, 11, 16
        val called = setOf(1, 6, 11, 16)
        val chosen = botEngine.chooseNumber(card, called)
        // Bot should strategically pick 21 to complete Column 0!
        assertEquals(21, chosen)
    }

    @Test
    fun testChooseNumber_allNumbersCalledReturnsNull() {
        val card = (1..25).toList()
        val called = (1..25).toSet()

        val chosen = botEngine.chooseNumber(card, called)
        assertNull(chosen)
    }
}
