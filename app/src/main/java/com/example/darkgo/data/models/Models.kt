package com.example.darkgo.data.models

enum class RoomStatus {
    WAITING,
    CARD_SELECTION,
    PLAYING,
    FINISHED,
    CANCELLED
}

data class Player(
    val uid: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val joinedAt: Long = 0L,
    val ready: Boolean = false,
    val connected: Boolean = true
)

data class GameState(
    val currentTurnUid: String = "",
    val turnIndex: Int = 0,
    val calledNumbers: List<Int> = emptyList(),
    val currentNumber: Int? = null,
    val winners: List<String> = emptyList(),
    val startedAt: Long = 0L,
    val endedAt: Long = 0L
)

data class Room(
    val roomId: String = "",
    val roomCode: String = "",
    val hostUid: String = "",
    val status: RoomStatus = RoomStatus.WAITING,
    val maxPlayers: Int = 10,
    val createdAt: Long = 0L,
    val players: Map<String, Player> = emptyMap(),
    val cards: Map<String, List<Int>> = emptyMap(),
    val game: GameState? = null
)

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val bio: String = "",
    val avatarId: String = "avatar_cyan",
    val createdAt: Long = 0L,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0
)

data class WinnerInfo(
    val uid: String,
    val displayName: String,
    val completedRows: Int = 5
)
