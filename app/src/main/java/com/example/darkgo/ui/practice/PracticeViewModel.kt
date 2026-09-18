package com.example.darkgo.ui.practice

import androidx.lifecycle.ViewModel
import com.example.darkgo.core.BingoEngine
import com.example.darkgo.data.models.WinnerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PracticeUiState(
    val playerCard: List<Int> = emptyList(),
    val calledNumbers: List<Int> = emptyList(),
    val currentNumber: Int? = null,
    val completedRowIndices: List<Int> = emptyList(),
    val completedLinesCount: Int = 0,
    val isGameFinished: Boolean = false,
    val winners: List<WinnerInfo> = emptyList()
)

class PracticeViewModel(
    private val bingoEngine: BingoEngine = BingoEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    fun startGame(card: List<Int>) {
        _uiState.value = PracticeUiState(
            playerCard = card,
            calledNumbers = emptyList(),
            currentNumber = null,
            completedRowIndices = emptyList(),
            completedLinesCount = 0,
            isGameFinished = false,
            winners = emptyList()
        )
    }

    fun callNumber(number: Int) {
        val current = _uiState.value
        if (current.isGameFinished) return
        if (number in current.calledNumbers) return

        val newCalled = current.calledNumbers + number
        val newCalledSet = newCalled.toSet()

        val completedRows = bingoEngine.getCompletedRowIndices(current.playerCard, newCalledSet)
        val completedLines = bingoEngine.completedLines(current.playerCard, newCalledSet)
        val hasBingo = bingoEngine.hasBingo(current.playerCard, newCalledSet)

        val winnersList = if (hasBingo) {
            listOf(WinnerInfo(uid = "practice_player", displayName = "You", completedRows = completedLines))
        } else {
            emptyList()
        }

        _uiState.value = current.copy(
            calledNumbers = newCalled,
            currentNumber = number,
            completedRowIndices = completedRows,
            completedLinesCount = completedLines,
            isGameFinished = hasBingo,
            winners = winnersList
        )
    }

    fun restart() {
        _uiState.value = PracticeUiState(playerCard = _uiState.value.playerCard)
    }
}
