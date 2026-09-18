package com.example.darkgo.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.darkgo.core.BingoEngine
import com.example.darkgo.data.models.Player
import com.example.darkgo.data.models.Room
import com.example.darkgo.data.models.RoomStatus
import com.example.darkgo.data.models.WinnerInfo
import com.example.darkgo.data.repository.RoomRepository
import com.example.darkgo.ui.game.PlayerStatusUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoomUiState(
    val currentRoom: Room? = null,
    val publicRooms: List<Map<String, Any>> = emptyList(),
    val isLoading: Boolean = false,
    val isActionProcessing: Boolean = false,
    val errorMessage: String? = null,
    val userCard: List<Int> = emptyList(),
    val userCompletedRows: List<Int> = emptyList(),
    val userCompletedLinesCount: Int = 0,
    val winnersList: List<WinnerInfo> = emptyList()
)

class RoomViewModel(
    private val roomRepository: RoomRepository = RoomRepository(),
    private val bingoEngine: BingoEngine = BingoEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    private var roomListenerJob: Job? = null
    private var publicRoomsJob: Job? = null

    init {
        startListeningToPublicRooms()
    }

    fun startListeningToPublicRooms() {
        publicRoomsJob?.cancel()
        publicRoomsJob = viewModelScope.launch {
            roomRepository.listenToPublicRooms().collect { list ->
                _uiState.value = _uiState.value.copy(publicRooms = list)
            }
        }
    }

    fun createRoom(player: Player, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = roomRepository.createRoom(
                hostUid = player.uid,
                hostDisplayName = player.displayName,
                hostPhotoUrl = player.photoUrl
            )
            result.onSuccess { room ->
                _uiState.value = _uiState.value.copy(isLoading = false, currentRoom = room)
                listenToRoom(room.roomId, player.uid)
                onSuccess(room.roomId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
            }
        }
    }

    fun joinRoomByCode(code: String, player: Player, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = roomRepository.joinRoomByCode(code, player)
            result.onSuccess { roomId ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                listenToRoom(roomId, player.uid)
                onSuccess(roomId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
            }
        }
    }

    fun joinRoomById(roomId: String, player: Player, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = roomRepository.joinRoomById(roomId, player)
            result.onSuccess { id ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                listenToRoom(id, player.uid)
                onSuccess(id)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
            }
        }
    }

    fun listenToRoom(roomId: String, currentUid: String) {
        roomListenerJob?.cancel()
        roomListenerJob = viewModelScope.launch {
            roomRepository.listenToRoom(roomId).collect { room ->
                if (room == null) {
                    _uiState.value = _uiState.value.copy(
                        currentRoom = null,
                        errorMessage = "Room closed or not found"
                    )
                    return@collect
                }

                // Retrieve player card if available
                val myCard = room.cards[currentUid] ?: _uiState.value.userCard
                val calledNumbers = room.game?.calledNumbers ?: emptyList()
                val calledSet = calledNumbers.toSet()

                val completedRows = if (myCard.size == 25) {
                    bingoEngine.getCompletedRowIndices(myCard, calledSet)
                } else {
                    emptyList()
                }
                val completedLinesCount = if (myCard.size == 25) {
                    bingoEngine.completedLines(myCard, calledSet)
                } else {
                    0
                }

                // Map winners if game ended
                val winners = if (room.status == RoomStatus.FINISHED && room.game?.winners != null) {
                    room.game.winners.map { winnerUid ->
                        val name = room.players[winnerUid]?.displayName ?: "Player"
                        WinnerInfo(uid = winnerUid, displayName = name, completedRows = 5)
                    }
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    currentRoom = room,
                    userCard = myCard,
                    userCompletedRows = completedRows,
                    userCompletedLinesCount = completedLinesCount,
                    winnersList = winners,
                    errorMessage = null
                )
            }
        }
    }

    fun submitCard(roomId: String, uid: String, card: List<Int>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionProcessing = true, errorMessage = null)
            val result = roomRepository.submitCardAndSetReady(roomId, uid, card)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isActionProcessing = false, userCard = card)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isActionProcessing = false, errorMessage = error.message)
            }
        }
    }

    fun startGame(roomId: String, hostUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionProcessing = true, errorMessage = null)
            val result = roomRepository.startGame(roomId, hostUid)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isActionProcessing = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isActionProcessing = false, errorMessage = error.message)
            }
        }
    }

    fun callNumber(roomId: String, uid: String, number: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionProcessing = true, errorMessage = null)
            val result = roomRepository.callNumber(roomId, uid, number)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isActionProcessing = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isActionProcessing = false, errorMessage = error.message)
            }
        }
    }

    fun leaveRoom(roomId: String, uid: String) {
        viewModelScope.launch {
            roomListenerJob?.cancel()
            roomRepository.leaveRoom(roomId, uid)
            _uiState.value = _uiState.value.copy(currentRoom = null, userCard = emptyList(), winnersList = emptyList())
        }
    }

    fun getPlayersStatus(currentRoom: Room): List<PlayerStatusUi> {
        val calledSet = currentRoom.game?.calledNumbers?.toSet() ?: emptySet()
        val currentTurnUid = currentRoom.game?.currentTurnUid

        return currentRoom.players.values.map { p ->
            val card = currentRoom.cards[p.uid] ?: emptyList()
            val lines = if (card.size == 25) bingoEngine.completedLines(card, calledSet) else 0
            PlayerStatusUi(
                name = p.displayName,
                isCurrentTurn = p.uid == currentTurnUid,
                isConnected = p.connected,
                completedRows = lines,
                completedLines = lines
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        roomListenerJob?.cancel()
        publicRoomsJob?.cancel()
    }
}
