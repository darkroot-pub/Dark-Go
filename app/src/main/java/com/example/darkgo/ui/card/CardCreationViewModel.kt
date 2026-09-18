package com.example.darkgo.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.darkgo.core.BingoEngine
import com.example.darkgo.core.CardValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardCreationUiState(
    val cells: List<Int?> = List(25) { null },
    val nextNumberToAssign: Int = 1,
    val history: List<Int> = emptyList(), // stores cell index history
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null,
    val validationResult: CardValidationResult? = null
)

class CardCreationViewModel(
    private val bingoEngine: BingoEngine = BingoEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardCreationUiState())
    val uiState: StateFlow<CardCreationUiState> = _uiState.asStateFlow()

    fun onCellTapped(index: Int) {
        val current = _uiState.value
        if (current.isConfirmed) return

        // If cell is already filled, we do nothing (or show tip)
        if (current.cells[index] != null) return

        if (current.nextNumberToAssign > 25) return

        val newCells = current.cells.toMutableList()
        val assignedNumber = current.nextNumberToAssign
        newCells[index] = assignedNumber

        val newHistory = current.history + index
        val nextNum = assignedNumber + 1

        _uiState.value = current.copy(
            cells = newCells,
            nextNumberToAssign = nextNum,
            history = newHistory,
            errorMessage = null
        )
    }

    fun undo() {
        val current = _uiState.value
        if (current.isConfirmed) return
        if (current.history.isEmpty()) return

        val lastCellIndex = current.history.last()
        val newCells = current.cells.toMutableList()
        newCells[lastCellIndex] = null

        val newHistory = current.history.dropLast(1)
        val newNextNumber = (current.nextNumberToAssign - 1).coerceAtLeast(1)

        _uiState.value = current.copy(
            cells = newCells,
            nextNumberToAssign = newNextNumber,
            history = newHistory,
            errorMessage = null,
            validationResult = null
        )
    }

    fun reset() {
        val current = _uiState.value
        if (current.isConfirmed) return

        _uiState.value = CardCreationUiState()
    }

    fun randomFill() {
        val current = _uiState.value
        if (current.isConfirmed) return

        val shuffled = bingoEngine.generateRandomCard()
        _uiState.value = current.copy(
            cells = shuffled,
            nextNumberToAssign = 26,
            history = (0..24).toList(),
            errorMessage = null,
            validationResult = CardValidationResult.Valid
        )
    }

    fun validateAndConfirm(): List<Int>? {
        val current = _uiState.value
        val filledNumbers = current.cells.filterNotNull()

        val validation = bingoEngine.validateCard(filledNumbers)
        if (validation !is CardValidationResult.Valid) {
            val errorMsg = when (validation) {
                is CardValidationResult.InvalidSize -> "Card must have 25 cells filled (${filledNumbers.size}/25 filled)"
                is CardValidationResult.DuplicateNumbers -> "Duplicate numbers found: ${validation.duplicates}"
                is CardValidationResult.OutOfRange -> "Invalid numbers out of 1..25 range"
                is CardValidationResult.MissingNumbers -> "Missing numbers: ${validation.missing}"
                else -> "Card is incomplete"
            }
            _uiState.value = current.copy(errorMessage = errorMsg, validationResult = validation)
            return null
        }

        _uiState.value = current.copy(isConfirmed = true, errorMessage = null, validationResult = validation)
        return filledNumbers
    }
}
