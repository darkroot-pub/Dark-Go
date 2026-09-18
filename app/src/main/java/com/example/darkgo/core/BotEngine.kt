package com.example.darkgo.core

/**
 * Strategy-driven Bot Engine for Dark Go.
 *
 * Rules:
 * - Bots hold valid 5x5 cards.
 * - Bots can only select uncalled numbers from 1..25.
 * - Bots have no hidden information beyond their own card and public called numbers.
 * - Strategy prioritizes uncalled numbers in straight lines (horizontal, vertical, diagonal)
 *   that are closest to completion.
 */
class BotEngine(private val bingoEngine: BingoEngine = BingoEngine()) {

    data class BotPlayer(
        val id: String,
        val name: String,
        val card: List<Int>,
        val avatarIndex: Int = 0
    )

    /**
     * Chooses an optimal uncalled number for the bot.
     * Evaluates all 12 straight lines (rows, columns, diagonals).
     * Scores each uncalled number by how close its intersecting lines are to completion
     * (e.g. lines with 4/5 called numbers get maximum weight).
     */
    fun chooseNumber(botCard: List<Int>, calledNumbers: Set<Int>): Int? {
        val availableNumbers = (1..BingoEngine.TOTAL_CELLS).filter { it !in calledNumbers }
        if (availableNumbers.isEmpty()) return null

        // Score map for each uncalled number on the bot's card
        val numberScores = mutableMapOf<Int, Int>()

        for ((_, indices) in BingoEngine.ALL_STRAIGHT_LINES) {
            val lineNumbers = indices.map { botCard[it] }
            val uncalledInLine = lineNumbers.filter { it !in calledNumbers }
            val calledCount = lineNumbers.size - uncalledInLine.size

            if (uncalledInLine.isNotEmpty() && calledCount < BingoEngine.GRID_SIZE) {
                // Weight is exponentially higher for lines near completion:
                // 4 called -> 100 pts, 3 called -> 20 pts, 2 called -> 5 pts, 1 called -> 1 pt
                val lineWeight = when (calledCount) {
                    4 -> 100
                    3 -> 20
                    2 -> 5
                    1 -> 1
                    else -> 0
                }

                for (num in uncalledInLine) {
                    numberScores[num] = (numberScores[num] ?: 0) + lineWeight
                }
            }
        }

        // Pick highest scored number
        val bestEntry = numberScores.maxByOrNull { it.value }
        if (bestEntry != null && bestEntry.value > 0) {
            return bestEntry.key
        }

        // Fallback: pick any remaining available number on bot's card or board
        val uncalledOnBotCard = botCard.filter { it !in calledNumbers }
        if (uncalledOnBotCard.isNotEmpty()) {
            return uncalledOnBotCard.random()
        }

        return availableNumbers.randomOrNull()
    }

    /**
     * Factory to instantiate N bot players with valid cards.
     */
    fun createBots(count: Int): List<BotPlayer> {
        val botNames = listOf("AlphaBot", "NexusBot", "ShadowBot", "QuantumBot")
        return (0 until count.coerceIn(1, 4)).map { index ->
            val name = botNames.getOrElse(index) { "Bot ${index + 1}" }
            BotPlayer(
                id = "bot_${index + 1}",
                name = name,
                card = bingoEngine.generateRandomCard(),
                avatarIndex = index
            )
        }
    }
}
