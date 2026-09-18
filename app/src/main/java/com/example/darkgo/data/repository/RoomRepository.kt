package com.example.darkgo.data.repository

import com.example.darkgo.core.BingoEngine
import com.example.darkgo.data.models.GameState
import com.example.darkgo.data.models.Player
import com.example.darkgo.data.models.Room
import com.example.darkgo.data.models.RoomStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class RoomRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://darkgo-72baf-default-rtdb.firebaseio.com"),
    private val bingoEngine: BingoEngine = BingoEngine()
) {

    private val roomsRef = database.reference.child("rooms")
    private val publicRoomsRef = database.reference.child("public_rooms")
    private val connectedRef = database.reference.child(".info/connected")

    /**
     * Creates a new multiplayer room with a unique 5-character alphanumeric room code.
     */
    suspend fun createRoom(
        hostUid: String,
        hostDisplayName: String,
        hostPhotoUrl: String?
    ): Result<Room> {
        return try {
            val roomId = roomsRef.push().key ?: throw Exception("Failed to generate room ID")
            val roomCode = generateRoomCode()
            val now = System.currentTimeMillis()

            val hostPlayer = Player(
                uid = hostUid,
                displayName = hostDisplayName,
                photoUrl = hostPhotoUrl,
                joinedAt = now,
                ready = false,
                connected = true
            )

            val room = Room(
                roomId = roomId,
                roomCode = roomCode,
                hostUid = hostUid,
                status = RoomStatus.WAITING,
                maxPlayers = 10,
                createdAt = now,
                players = mapOf(hostUid to hostPlayer),
                cards = emptyMap(),
                game = null
            )

            // Write to rooms and public_rooms
            roomsRef.child(roomId).setValue(serializeRoom(room)).await()
            publicRoomsRef.child(roomId).setValue(
                mapOf(
                    "roomId" to roomId,
                    "roomCode" to roomCode,
                    "status" to RoomStatus.WAITING.name,
                    "playerCount" to 1,
                    "maxPlayers" to 10,
                    "createdAt" to now
                )
            ).await()

            // Setup presence
            setupPresence(roomId, hostUid)

            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Joins a room using its 5-character room code with atomic player limit validation.
     */
    suspend fun joinRoomByCode(
        roomCode: String,
        userPlayer: Player
    ): Result<String> {
        return try {
            val querySnapshot = roomsRef.orderByChild("roomCode").equalTo(roomCode.trim().uppercase()).get().await()
            if (!querySnapshot.exists() || querySnapshot.childrenCount == 0L) {
                return Result.failure(Exception("Room with code '${roomCode.trim().uppercase()}' not found"))
            }

            val targetRoomSnapshot = querySnapshot.children.first()
            val roomId = targetRoomSnapshot.key ?: throw Exception("Invalid room data")

            joinRoomById(roomId, userPlayer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Joins a room atomically ensuring maximum 10 players and room in WAITING state.
     */
    suspend fun joinRoomById(
        roomId: String,
        userPlayer: Player
    ): Result<String> {
        val roomTargetRef = roomsRef.child(roomId)

        // Warm the local cache with a fresh read before running the transaction.
        // Without this, if this client has no active listener on this exact path yet
        // (e.g. the very first time it touches this room), the transaction's first
        // invocation can see a null/stale local snapshot and abort immediately with
        // a false "room full or already started" error -- even though the room is
        // fine. Retrying used to work only because the failed attempt happened to
        // warm the cache for next time. Fetching first fixes it on the first try.
        try {
            roomTargetRef.get().await()
        } catch (_: Exception) {
            // Ignore -- the transaction below will still run off whatever data is
            // available (offline cache or a subsequent retry from the SDK).
        }

        return suspendCancellableCoroutine { continuation ->
            roomTargetRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == null) {
                    return Transaction.abort()
                }

                val status = currentData.child("status").getValue(String::class.java)
                if (status != RoomStatus.WAITING.name) {
                    return Transaction.abort()
                }

                val playersData = currentData.child("players")
                val currentCount = playersData.childrenCount
                val isAlreadyIn = playersData.hasChild(userPlayer.uid)

                if (!isAlreadyIn && currentCount >= 10) {
                    // Room is full! Reject 11th player.
                    return Transaction.abort()
                }

                // Add or update player
                val playerNode = playersData.child(userPlayer.uid)
                playerNode.child("uid").value = userPlayer.uid
                playerNode.child("displayName").value = userPlayer.displayName
                playerNode.child("photoUrl").value = userPlayer.photoUrl
                playerNode.child("joinedAt").value = System.currentTimeMillis()
                playerNode.child("ready").value = false
                playerNode.child("connected").value = true

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.message)))
                } else if (!committed) {
                    continuation.resume(Result.failure(Exception("Cannot join: Room is full (max 10) or already started")))
                } else {
                    setupPresence(roomId, userPlayer.uid)
                    // Update public room player count
                    val playerCount = currentData?.child("players")?.childrenCount ?: 1
                    publicRoomsRef.child(roomId).child("playerCount").setValue(playerCount)
                    continuation.resume(Result.success(roomId))
                }
            }
            })
        }
    }

    /**
     * Submits a 5x5 card for the player and marks readiness.
     */
    suspend fun submitCardAndSetReady(
        roomId: String,
        uid: String,
        cardNumbers: List<Int>
    ): Result<Unit> {
        val validation = bingoEngine.validateCard(cardNumbers)
        if (validation !is com.example.darkgo.core.CardValidationResult.Valid) {
            return Result.failure(Exception("Invalid card: Must contain unique numbers 1..25"))
        }

        return try {
            roomsRef.child(roomId).child("cards").child(uid).setValue(cardNumbers).await()
            roomsRef.child(roomId).child("players").child(uid).child("ready").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Host starts the game when min 2 players are in the room and all are ready.
     */
    suspend fun startGame(roomId: String, hostUid: String): Result<Unit> {
        val roomRef = roomsRef.child(roomId)
        try {
            roomRef.get().await()
        } catch (_: Exception) {
            // Ignore -- see joinRoomById for why this warm-up read exists.
        }

        return suspendCancellableCoroutine { continuation ->
            roomRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    if (currentData.value == null) return Transaction.abort()

                    val actualHost = currentData.child("hostUid").getValue(String::class.java)
                    if (actualHost != hostUid) return Transaction.abort()

                    val playersData = currentData.child("players")
                    val cardsData = currentData.child("cards")
                    val playerKeys = playersData.children.mapNotNull { it.key }

                    if (playerKeys.size < 2) return Transaction.abort()

                    // Verify all players are ready and have submitted cards
                    val allReady = playerKeys.all { uid ->
                        val isReady = playersData.child(uid).child("ready").getValue(Boolean::class.java) ?: false
                        val hasCard = cardsData.hasChild(uid)
                        isReady && hasCard
                    }

                    if (!allReady) return Transaction.abort()

                    // Start game! Select first player
                    val firstTurnUid = playerKeys.first()
                    currentData.child("status").value = RoomStatus.PLAYING.name
                    val gameNode = currentData.child("game")
                    gameNode.child("currentTurnUid").value = firstTurnUid
                    gameNode.child("turnIndex").value = 0
                    gameNode.child("calledNumbers").value = emptyList<Int>()
                    gameNode.child("currentNumber").value = null
                    gameNode.child("winners").value = emptyList<String>()
                    gameNode.child("startedAt").value = System.currentTimeMillis()
                    gameNode.child("endedAt").value = 0L

                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    } else if (!committed) {
                        continuation.resume(Result.failure(Exception("Cannot start: At least 2 ready players with confirmed cards required")))
                    } else {
                        // Remove from public room list now that it is PLAYING
                        publicRoomsRef.child(roomId).removeValue()
                        continuation.resume(Result.success(Unit))
                    }
                }
            })
        }
    }

    /**
     * Authoritatively calls a number in the game state.
     * Validates current turn, number range, uncalled status, checks Bingo for all cards,
     * supports simultaneous multiple winners, and advances turn cleanly.
     */
    suspend fun callNumber(
        roomId: String,
        callingUid: String,
        number: Int
    ): Result<Unit> {
        if (number !in 1..25) {
            return Result.failure(Exception("Number must be between 1 and 25"))
        }

        val roomRef = roomsRef.child(roomId)
        try {
            roomRef.get().await()
        } catch (_: Exception) {
            // Ignore -- see joinRoomById for why this warm-up read exists.
        }

        return suspendCancellableCoroutine { continuation ->
        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == null) return Transaction.abort()

                val status = currentData.child("status").getValue(String::class.java)
                if (status != RoomStatus.PLAYING.name) return Transaction.abort()

                val gameNode = currentData.child("game")
                val currentTurnUid = gameNode.child("currentTurnUid").getValue(String::class.java)
                if (currentTurnUid != callingUid) {
                    // Not your turn!
                    return Transaction.abort()
                }

                // Read called numbers
                val rawCalled = gameNode.child("calledNumbers").children.mapNotNull { it.getValue(Long::class.java)?.toInt() }
                if (rawCalled.contains(number)) {
                    // Number already called!
                    return Transaction.abort()
                }

                val newCalledList = rawCalled + number
                val newCalledSet = newCalledList.toSet()

                gameNode.child("calledNumbers").value = newCalledList
                gameNode.child("currentNumber").value = number

                // Check Bingo for ALL players' cards
                val cardsNode = currentData.child("cards")
                val playersNode = currentData.child("players")
                val allPlayerUids = playersNode.children.mapNotNull { it.key }
                val winners = mutableListOf<String>()

                for (uid in allPlayerUids) {
                    val cardRaw = cardsNode.child(uid).children.mapNotNull { it.getValue(Long::class.java)?.toInt() }
                    if (cardRaw.size == 25) {
                        if (bingoEngine.hasBingo(cardRaw, newCalledSet)) {
                            winners.add(uid)
                        }
                    }
                }

                if (winners.isNotEmpty()) {
                    // Game finished! One or more simultaneous winners!
                    currentData.child("status").value = RoomStatus.FINISHED.name
                    gameNode.child("winners").value = winners
                    gameNode.child("endedAt").value = System.currentTimeMillis()
                } else {
                    // Advance to next active connected player
                    val currentTurnIndex = gameNode.child("turnIndex").getValue(Long::class.java)?.toInt() ?: 0
                    val nextIndex = (currentTurnIndex + 1) % allPlayerUids.size
                    val nextUid = allPlayerUids[nextIndex]

                    gameNode.child("turnIndex").value = nextIndex
                    gameNode.child("currentTurnUid").value = nextUid
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.message)))
                } else if (!committed) {
                    continuation.resume(Result.failure(Exception("Number call failed: Not your turn or number already called")))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        })
        }
    }

    /**
     * Leaves the room and migrates host if host leaves during WAITING.
     */
    suspend fun leaveRoom(roomId: String, uid: String) {
        try {
            val roomSnapshot = roomsRef.child(roomId).get().await()
            if (!roomSnapshot.exists()) return

            val hostUid = roomSnapshot.child("hostUid").getValue(String::class.java)
            val status = roomSnapshot.child("status").getValue(String::class.java)
            val playersNode = roomSnapshot.child("players")

            // Remove player from room
            roomsRef.child(roomId).child("players").child(uid).removeValue().await()
            roomsRef.child(roomId).child("cards").child(uid).removeValue().await()

            val remainingPlayers = playersNode.children.mapNotNull { it.key }.filter { it != uid }

            if (remainingPlayers.isEmpty()) {
                // Room empty -> delete
                roomsRef.child(roomId).removeValue().await()
                publicRoomsRef.child(roomId).removeValue().await()
            } else {
                // If host left in WAITING state, migrate host to next remaining player
                if (hostUid == uid && status == RoomStatus.WAITING.name) {
                    val newHostUid = remainingPlayers.first()
                    roomsRef.child(roomId).child("hostUid").setValue(newHostUid).await()
                }
                // Update public rooms player count
                publicRoomsRef.child(roomId).child("playerCount").setValue(remainingPlayers.size).await()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Configures Firebase Realtime Database onDisconnect presence.
     */
    private fun setupPresence(roomId: String, uid: String) {
        val playerConnectedRef = roomsRef.child(roomId).child("players").child(uid).child("connected")
        val presenceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    playerConnectedRef.onDisconnect().setValue(false)
                    playerConnectedRef.setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        connectedRef.addValueEventListener(presenceListener)
    }

    /**
     * Flow listening to the authoritative room state.
     */
    fun listenToRoom(roomId: String): Flow<Room?> = callbackFlow {
        val roomRef = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val room = deserializeRoom(snapshot)
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomRef.addValueEventListener(listener)
        awaitClose { roomRef.removeEventListener(listener) }
    }

    /**
     * Flow listening to public waiting rooms.
     */
    fun listenToPublicRooms(): Flow<List<Map<String, Any>>> = callbackFlow {
        val query = publicRoomsRef.limitToLast(20)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<*, *>
                    if (map != null) {
                        @Suppress("UNCHECKED_CAST")
                        list.add(map as Map<String, Any>)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..5).map { chars.random() }.joinToString("")
    }

    private fun serializeRoom(room: Room): Map<String, Any?> {
        return mapOf(
            "roomId" to room.roomId,
            "roomCode" to room.roomCode,
            "hostUid" to room.hostUid,
            "status" to room.status.name,
            "maxPlayers" to room.maxPlayers,
            "createdAt" to room.createdAt,
            "players" to room.players.mapValues { (_, p) ->
                mapOf(
                    "uid" to p.uid,
                    "displayName" to p.displayName,
                    "photoUrl" to p.photoUrl,
                    "joinedAt" to p.joinedAt,
                    "ready" to p.ready,
                    "connected" to p.connected
                )
            },
            "cards" to room.cards
        )
    }

    private fun deserializeRoom(snapshot: DataSnapshot): Room {
        val roomId = snapshot.child("roomId").getValue(String::class.java) ?: snapshot.key.orEmpty()
        val roomCode = snapshot.child("roomCode").getValue(String::class.java).orEmpty()
        val hostUid = snapshot.child("hostUid").getValue(String::class.java).orEmpty()
        val statusStr = snapshot.child("status").getValue(String::class.java) ?: RoomStatus.WAITING.name
        val status = try { RoomStatus.valueOf(statusStr) } catch (_: Exception) { RoomStatus.WAITING }
        val maxPlayers = snapshot.child("maxPlayers").getValue(Long::class.java)?.toInt() ?: 10
        val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L

        val playersMap = mutableMapOf<String, Player>()
        for (pSnap in snapshot.child("players").children) {
            val uid = pSnap.child("uid").getValue(String::class.java) ?: pSnap.key.orEmpty()
            val displayName = pSnap.child("displayName").getValue(String::class.java) ?: "Player"
            val photoUrl = pSnap.child("photoUrl").getValue(String::class.java)
            val joinedAt = pSnap.child("joinedAt").getValue(Long::class.java) ?: 0L
            val ready = pSnap.child("ready").getValue(Boolean::class.java) ?: false
            val connected = pSnap.child("connected").getValue(Boolean::class.java) ?: true
            playersMap[uid] = Player(uid, displayName, photoUrl, joinedAt, ready, connected)
        }

        val cardsMap = mutableMapOf<String, List<Int>>()
        for (cSnap in snapshot.child("cards").children) {
            val uid = cSnap.key.orEmpty()
            val numbers = cSnap.children.mapNotNull { it.getValue(Long::class.java)?.toInt() }
            cardsMap[uid] = numbers
        }

        var gameState: GameState? = null
        val gameSnap = snapshot.child("game")
        if (gameSnap.exists()) {
            val currentTurnUid = gameSnap.child("currentTurnUid").getValue(String::class.java).orEmpty()
            val turnIndex = gameSnap.child("turnIndex").getValue(Long::class.java)?.toInt() ?: 0
            val calledNumbers = gameSnap.child("calledNumbers").children.mapNotNull { it.getValue(Long::class.java)?.toInt() }
            val currentNumber = gameSnap.child("currentNumber").getValue(Long::class.java)?.toInt()
            val winners = gameSnap.child("winners").children.mapNotNull { it.getValue(String::class.java) }
            val startedAt = gameSnap.child("startedAt").getValue(Long::class.java) ?: 0L
            val endedAt = gameSnap.child("endedAt").getValue(Long::class.java) ?: 0L
            gameState = GameState(currentTurnUid, turnIndex, calledNumbers, currentNumber, winners, startedAt, endedAt)
        }

        return Room(roomId, roomCode, hostUid, status, maxPlayers, createdAt, playersMap, cardsMap, gameState)
    }
}
