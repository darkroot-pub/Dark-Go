package com.example.darkgo.ui.bots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.darkgo.core.BingoEngine
import com.example.darkgo.core.BotEngine
import com.example.darkgo.data.models.WinnerInfo
import com.example.darkgo.ui.game.PlayerStatusUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BotGameUiState(
    val playerCard: List<Int> = emptyList(),
    val bots: List<BotEngine.BotPlayer> = emptyList(),
    val calledNumbers: List<Int> = emptyList(),
    val currentNumber: Int? = null,
    val turnOrder: List<String> = emptyList(), // "user", "bot_1", etc.
    val currentTurnIndex: Int = 0,
    val completedRowIndices: List<Int> = emptyList(),
    val completedLinesCount: Int = 0,
    val isGameFinished: Boolean = false,
    val winners: List<WinnerInfo> = emptyList(),
    val isBotThinking: Boolean = false
)

class BotGameViewModel(
    private val bingoEngine: BingoEngine = BingoEngine(),
    private val botEngine: BotEngine = BotEngine(bingoEngine)
) : ViewModel() {

    private val _uiState = MutableStateFlow(BotGameUiState())
    val uiState: StateFlow<BotGameUiState> = _uiState.asStateFlow()

    private var botTurnJob: Job? = null

    fun startBotGame(playerCard: List<Int>, botCount: Int) {
        botTurnJob?.cancel()
        val bots = botEngine.createBots(botCount)
        val turnOrder = listOf("user") + bots.map { it.id }

        _uiState.value = BotGameUiState(
            playerCard = playerCard,
            bots = bots,
            calledNumbers = emptyList(),
            currentNumber = null,
            turnOrder = turnOrder,
            currentTurnIndex = 0,
            completedRowIndices = emptyList(),
            completedLinesCount = 0,
            isGameFinished = false,
            winners = emptyList(),
            isBotThinking = false
        )
    }

    fun callUserNumber(number: Int) {
        val current = _uiState.value
        if (current.isGameFinished || current.isBotThinking) return
        if (current.turnOrder.getOrNull(current.currentTurnIndex) != "user") return
        if (number in current.calledNumbers) return

        processNumberCall(number)
    }

    private fun processNumberCall(number: Int) {
        val current = _uiState.value
        val newCalled = current.calledNumbers + number
        val newCalledSet = newCalled.toSet()

        // Check player lines
        val userCompletedRows = bingoEngine.getCompletedRowIndices(current.playerCard, newCalledSet)
        val userCompletedLines = bingoEngine.completedLines(current.playerCard, newCalledSet)
        val userBingo = bingoEngine.hasBingo(current.playerCard, newCalledSet)

        // Check bots lines
        val winners = mutableListOf<WinnerInfo>()
        if (userBingo) {
            winners.add(WinnerInfo(uid = "user", displayName = "You", completedRows = userCompletedLines))
        }

        for (bot in current.bots) {
            val botBingo = bingoEngine.hasBingo(bot.card, newCalledSet)
            val botLines = bingoEngine.completedLines(bot.card, newCalledSet)
            if (botBingo) {
                winners.add(WinnerInfo(uid = bot.id, displayName = bot.name, completedRows = botLines))
            }
        }

        if (winners.isNotEmpty()) {
            // Finished! One or more winners
            _uiState.value = current.copy(
                calledNumbers = newCalled,
                currentNumber = number,
                completedRowIndices = userCompletedRows,
                completedLinesCount = userCompletedLines,
                isGameFinished = true,
                winners = winners,
                isBotThinking = false
            )
            return
        }

        // Advance turn
        val nextIndex = (current.currentTurnIndex + 1) % current.turnOrder.size
        val nextTurnPlayerId = current.turnOrder[nextIndex]

        _uiState.value = current.copy(
            calledNumbers = newCalled,
            currentNumber = number,
            completedRowIndices = userCompletedRows,
            completedLinesCount = userCompletedLines,
            currentTurnIndex = nextIndex,
            isBotThinking = nextTurnPlayerId != "user"
        )

        // If next is bot, trigger bot decision
        if (nextTurnPlayerId != "user") {
            triggerBotTurn(nextTurnPlayerId)
        }
    }

    private fun triggerBotTurn(botId: String) {
        botTurnJob?.cancel()
        botTurnJob = viewModelScope.launch {
            // Realistic bot think delay (1000 - 1500 ms)
            delay(1200)

            val current = _uiState.value
            if (current.isGameFinished) return@launch

            val bot = current.bots.find { it.id == botId } ?: return@launch
            val chosenNumber = botEngine.chooseNumber(bot.card, current.calledNumbers.toSet())

            if (chosenNumber != null) {
                processNumberCall(chosenNumber)
            }
        }
    }

    fun getPlayerStatusList(): List<PlayerStatusUi> {
        val current = _uiState.value
        val calledSet = current.calledNumbers.toSet()
        val currentTurnId = current.turnOrder.getOrNull(current.currentTurnIndex)

        val userLines = if (current.completedLinesCount > 0) current.completedLinesCount else current.completedRowIndices.size
        val userStatus = PlayerStatusUi(
            name = "You",
            isCurrentTurn = currentTurnId == "user",
            isConnected = true,
            completedRows = userLines,
            completedLines = userLines
        )

        val botStatuses = current.bots.map { bot ->
            val lines = bingoEngine.completedLines(bot.card, calledSet)
            PlayerStatusUi(
                name = bot.name,
                isCurrentTurn = currentTurnId == bot.id,
                isConnected = true,
                completedRows = lines,
                completedLines = lines
            )
        }

        return listOf(userStatus) + botStatuses
    }

    fun restart() {
        botTurnJob?.cancel()
        startBotGame(_uiState.value.playerCard, _uiState.value.bots.size)
    }

    override fun onCleared() {
        super.onCleared()
        botTurnJob?.cancel()
    }
}
