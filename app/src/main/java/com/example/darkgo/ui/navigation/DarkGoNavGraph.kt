package com.example.darkgo.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.darkgo.data.models.Player
import com.example.darkgo.data.models.RoomStatus
import com.example.darkgo.ui.audio.SoundAndHapticManager
import com.example.darkgo.ui.auth.AuthViewModel
import com.example.darkgo.ui.auth.LoginScreen
import com.example.darkgo.ui.auth.RegisterScreen
import com.example.darkgo.ui.bots.BotGameUiState
import com.example.darkgo.ui.bots.BotGameViewModel
import com.example.darkgo.ui.bots.BotSetupScreen
import com.example.darkgo.ui.card.CardCreationScreen
import com.example.darkgo.ui.card.CardCreationViewModel
import com.example.darkgo.ui.game.GameScreen
import com.example.darkgo.ui.game.ResultsScreen
import com.example.darkgo.ui.home.HomeScreen
import com.example.darkgo.ui.home.HowToPlayDialog
import com.example.darkgo.ui.home.ProfileScreen
import com.example.darkgo.ui.home.SettingsScreen
import com.example.darkgo.ui.practice.PracticeViewModel
import com.example.darkgo.ui.room.CreateOrJoinRoomScreen
import com.example.darkgo.ui.room.LobbyScreen
import com.example.darkgo.ui.room.RoomViewModel
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoPrimary

object DarkGoDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_JOIN_ROOM = "create_join_room"
    const val LOBBY = "lobby"
    const val CARD_CREATION = "card_creation"
    const val ONLINE_GAME = "online_game"
    const val PRACTICE_GAME = "practice_game"
    const val BOT_SETUP = "bot_setup"
    const val BOT_GAME = "bot_game"
    const val RESULTS = "results"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
}

@Composable
fun DarkGoNavGraph(
    soundManager: SoundAndHapticManager,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    practiceViewModel: PracticeViewModel = viewModel(),
    botGameViewModel: BotGameViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val roomState by roomViewModel.uiState.collectAsState()

    var cardCreationMode by remember { mutableStateOf("online") } // "online", "practice", "bots"
    var configuredBotCount by remember { mutableIntStateOf(2) }
    var showHowToPlay by remember { mutableStateOf(false) }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    val startDestination = if (authState.currentUser != null) {
        DarkGoDestinations.HOME
    } else {
        DarkGoDestinations.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(DarkGoDestinations.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(DarkGoDestinations.HOME) {
                        popUpTo(DarkGoDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(DarkGoDestinations.REGISTER)
                },
                onGuestOrPractice = {
                    cardCreationMode = "practice"
                    navController.navigate(DarkGoDestinations.CARD_CREATION)
                }
            )
        }

        composable(DarkGoDestinations.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(DarkGoDestinations.HOME) {
                        popUpTo(DarkGoDestinations.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(DarkGoDestinations.HOME) {
            HomeScreen(
                userProfile = authState.userProfile,
                onPlayOnline = {
                    navController.navigate(DarkGoDestinations.CREATE_JOIN_ROOM)
                },
                onPractice = {
                    cardCreationMode = "practice"
                    navController.navigate(DarkGoDestinations.CARD_CREATION)
                },
                onVsBots = {
                    navController.navigate(DarkGoDestinations.BOT_SETUP)
                },
                onHowToPlay = {
                    showHowToPlay = true
                },
                onSettings = {
                    navController.navigate(DarkGoDestinations.SETTINGS)
                },
                onProfile = {
                    navController.navigate(DarkGoDestinations.PROFILE)
                },
                onLogout = {
                    authViewModel.signOut {
                        navController.navigate(DarkGoDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(DarkGoDestinations.CREATE_JOIN_ROOM) {
            val user = authState.currentUser
            val profile = authState.userProfile
            val player = Player(
                uid = user?.uid ?: "guest_${System.currentTimeMillis() % 10000}",
                displayName = profile?.displayName ?: user?.displayName ?: "Player"
            )

            val navigateHome: () -> Unit = {
                navController.navigate(DarkGoDestinations.HOME) {
                    popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }

            BackHandler(onBack = navigateHome)

            CreateOrJoinRoomScreen(
                publicRooms = roomState.publicRooms,
                isLoading = roomState.isLoading,
                errorMessage = roomState.errorMessage,
                onCreateRoom = {
                    roomViewModel.createRoom(player) {
                        navController.navigate(DarkGoDestinations.LOBBY)
                    }
                },
                onJoinByCode = { code ->
                    roomViewModel.joinRoomByCode(code, player) {
                        navController.navigate(DarkGoDestinations.LOBBY)
                    }
                },
                onJoinPublicRoom = { roomId ->
                    roomViewModel.joinRoomById(roomId, player) {
                        navController.navigate(DarkGoDestinations.LOBBY)
                    }
                },
                onBack = navigateHome
            )
        }

        composable(DarkGoDestinations.LOBBY) {
            val currentRoom = roomState.currentRoom
            val currentUid = authState.currentUser?.uid ?: ""

            val leaveLobbyAndNavigateBack: () -> Unit = {
                currentRoom?.let { room ->
                    roomViewModel.leaveRoom(room.roomId, currentUid)
                }
                val popped = navController.popBackStack(DarkGoDestinations.CREATE_JOIN_ROOM, inclusive = false)
                if (!popped) {
                    navController.navigate(DarkGoDestinations.HOME) {
                        popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            BackHandler(onBack = leaveLobbyAndNavigateBack)

            LaunchedEffect(currentRoom?.status) {
                if (currentRoom?.status == RoomStatus.PLAYING) {
                    navController.navigate(DarkGoDestinations.ONLINE_GAME) {
                        popUpTo(DarkGoDestinations.LOBBY) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(currentRoom) {
                if (currentRoom == null) {
                    val popped = navController.popBackStack(DarkGoDestinations.CREATE_JOIN_ROOM, inclusive = false)
                    if (!popped) {
                        navController.navigate(DarkGoDestinations.HOME) {
                            popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            }

            if (currentRoom != null) {
                LobbyScreen(
                    room = currentRoom,
                    currentUid = currentUid,
                    isProcessing = roomState.isActionProcessing,
                    errorMessage = roomState.errorMessage,
                    onCreateCardClick = {
                        cardCreationMode = "online"
                        navController.navigate(DarkGoDestinations.CARD_CREATION)
                    },
                    onStartGameClick = {
                        roomViewModel.startGame(currentRoom.roomId, currentUid)
                    },
                    onLeaveRoomClick = leaveLobbyAndNavigateBack
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkGoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkGoPrimary)
                }
            }
        }

        composable(DarkGoDestinations.CARD_CREATION) {
            val cardCreationViewModel: CardCreationViewModel = viewModel()
            val title = when (cardCreationMode) {
                "online" -> "Multiplayer Card"
                "bots" -> "Vs Bots Card"
                else -> "Practice Card"
            }

            BackHandler {
                navController.popBackStack()
            }

            CardCreationScreen(
                viewModel = cardCreationViewModel,
                soundManager = soundManager,
                title = title,
                onConfirm = { validatedCard ->
                    when (cardCreationMode) {
                        "online" -> {
                            val roomId = roomState.currentRoom?.roomId
                            val uid = authState.currentUser?.uid
                            if (roomId != null && uid != null) {
                                roomViewModel.submitCard(roomId, uid, validatedCard)
                            }
                            navController.popBackStack()
                        }
                        "practice" -> {
                            practiceViewModel.startGame(validatedCard)
                            navController.navigate(DarkGoDestinations.PRACTICE_GAME) {
                                popUpTo(DarkGoDestinations.CARD_CREATION) { inclusive = true }
                            }
                        }
                        "bots" -> {
                            botGameViewModel.startBotGame(validatedCard, configuredBotCount)
                            navController.navigate(DarkGoDestinations.BOT_GAME) {
                                popUpTo(DarkGoDestinations.CARD_CREATION) { inclusive = true }
                            }
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(DarkGoDestinations.ONLINE_GAME) {
            val currentRoom = roomState.currentRoom
            val currentUid = authState.currentUser?.uid ?: ""

            val exitOnlineGame: () -> Unit = {
                currentRoom?.let { room ->
                    roomViewModel.leaveRoom(room.roomId, currentUid)
                }
                navController.navigate(DarkGoDestinations.HOME) {
                    popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }

            BackHandler(onBack = exitOnlineGame)

            LaunchedEffect(currentRoom?.status) {
                if (currentRoom?.status == RoomStatus.FINISHED) {
                    soundManager.playBingoFanfare()
                }
            }

            LaunchedEffect(currentRoom) {
                if (currentRoom == null) {
                    navController.navigate(DarkGoDestinations.HOME) {
                        popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            if (currentRoom != null && currentRoom.status == RoomStatus.FINISHED) {
                ResultsScreen(
                    winners = roomState.winnersList,
                    totalCalledNumbers = currentRoom.game?.calledNumbers?.size ?: 0,
                    calledNumbersList = currentRoom.game?.calledNumbers ?: emptyList(),
                    playerCard = roomState.userCard,
                    completedRowsCount = roomState.userCompletedLinesCount,
                    onPlayAgain = exitOnlineGame,
                    onHome = exitOnlineGame
                )
            } else if (currentRoom != null) {
                val game = currentRoom.game
                val isMyTurn = game?.currentTurnUid == currentUid
                val currentTurnName = currentRoom.players[game?.currentTurnUid]?.displayName ?: "Opponent"

                GameScreen(
                    roomCodeOrTitle = "ROOM ${currentRoom.roomCode}",
                    isConnected = true,
                    isMyTurn = isMyTurn,
                    currentTurnPlayerName = currentTurnName,
                    currentCalledNumber = game?.currentNumber,
                    calledNumbers = game?.calledNumbers ?: emptyList(),
                    playerCard = roomState.userCard,
                    completedRowIndices = roomState.userCompletedRows,
                    completedLinesCount = roomState.userCompletedLinesCount,
                    playersStatus = roomViewModel.getPlayersStatus(currentRoom),
                    soundManager = soundManager,
                    isProcessing = roomState.isActionProcessing,
                    errorMessage = roomState.errorMessage,
                    onCallNumber = { num ->
                        roomViewModel.callNumber(currentRoom.roomId, currentUid, num)
                    },
                    onExitGame = exitOnlineGame
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkGoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkGoPrimary)
                }
            }
        }

        composable(DarkGoDestinations.PRACTICE_GAME) {
            val practiceState by practiceViewModel.uiState.collectAsState()

            val exitPracticeGame: () -> Unit = {
                navController.navigate(DarkGoDestinations.HOME) {
                    popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }

            BackHandler(onBack = exitPracticeGame)

            LaunchedEffect(practiceState.isGameFinished) {
                if (practiceState.isGameFinished) {
                    soundManager.playBingoFanfare()
                }
            }

            if (practiceState.isGameFinished) {
                ResultsScreen(
                    winners = practiceState.winners,
                    totalCalledNumbers = practiceState.calledNumbers.size,
                    calledNumbersList = practiceState.calledNumbers,
                    playerCard = practiceState.playerCard,
                    completedRowsCount = practiceState.completedLinesCount,
                    onPlayAgain = {
                        cardCreationMode = "practice"
                        navController.navigate(DarkGoDestinations.CARD_CREATION) {
                            popUpTo(DarkGoDestinations.PRACTICE_GAME) { inclusive = true }
                        }
                    },
                    onHome = exitPracticeGame
                )
            } else {
                GameScreen(
                    roomCodeOrTitle = "PRACTICE MODE",
                    isConnected = true,
                    isMyTurn = true,
                    currentTurnPlayerName = "You",
                    currentCalledNumber = practiceState.currentNumber,
                    calledNumbers = practiceState.calledNumbers,
                    playerCard = practiceState.playerCard,
                    completedRowIndices = practiceState.completedRowIndices,
                    completedLinesCount = practiceState.completedLinesCount,
                    playersStatus = emptyList(),
                    soundManager = soundManager,
                    isProcessing = false,
                    errorMessage = null,
                    onCallNumber = { num ->
                        practiceViewModel.callNumber(num)
                    },
                    onExitGame = exitPracticeGame
                )
            }
        }

        composable(DarkGoDestinations.BOT_SETUP) {
            BackHandler {
                navController.popBackStack()
            }

            BotSetupScreen(
                onStartBotMatch = { count ->
                    configuredBotCount = count
                    cardCreationMode = "bots"
                    navController.navigate(DarkGoDestinations.CARD_CREATION)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(DarkGoDestinations.BOT_GAME) {
            val botState by botGameViewModel.uiState.collectAsState()
            val isMyTurn = botState.turnOrder.getOrNull(botState.currentTurnIndex) == "user"
            val currentTurnId = botState.turnOrder.getOrNull(botState.currentTurnIndex)
            val currentTurnName = if (isMyTurn) "You" else botState.bots.find { it.id == currentTurnId }?.name ?: "Bot"

            val exitBotGame: () -> Unit = {
                navController.navigate(DarkGoDestinations.HOME) {
                    popUpTo(DarkGoDestinations.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }

            BackHandler(onBack = exitBotGame)

            LaunchedEffect(botState.isGameFinished) {
                if (botState.isGameFinished) {
                    soundManager.playBingoFanfare()
                }
            }

            if (botState.isGameFinished) {
                ResultsScreen(
                    winners = botState.winners,
                    totalCalledNumbers = botState.calledNumbers.size,
                    calledNumbersList = botState.calledNumbers,
                    playerCard = botState.playerCard,
                    completedRowsCount = botState.completedLinesCount,
                    onPlayAgain = {
                        cardCreationMode = "bots"
                        navController.navigate(DarkGoDestinations.CARD_CREATION) {
                            popUpTo(DarkGoDestinations.BOT_GAME) { inclusive = true }
                        }
                    },
                    onHome = exitBotGame
                )
            } else {
                GameScreen(
                    roomCodeOrTitle = "VS BOTS",
                    isConnected = true,
                    isMyTurn = isMyTurn,
                    currentTurnPlayerName = currentTurnName,
                    currentCalledNumber = botState.currentNumber,
                    calledNumbers = botState.calledNumbers,
                    playerCard = botState.playerCard,
                    completedRowIndices = botState.completedRowIndices,
                    completedLinesCount = botState.completedLinesCount,
                    playersStatus = botGameViewModel.getPlayerStatusList(),
                    soundManager = soundManager,
                    isProcessing = botState.isBotThinking,
                    errorMessage = null,
                    onCallNumber = { num ->
                        botGameViewModel.callUserNumber(num)
                    },
                    onExitGame = exitBotGame
                )
            }
        }

        composable(DarkGoDestinations.SETTINGS) {
            BackHandler {
                navController.popBackStack()
            }

            SettingsScreen(
                soundManager = soundManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(DarkGoDestinations.PROFILE) {
            BackHandler {
                navController.popBackStack()
            }

            ProfileScreen(
                userProfile = authState.userProfile,
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                successMessage = authState.successMessage,
                onSaveProfile = { name, bio, avatarId ->
                    authViewModel.updateProfile(name, bio, avatarId) {}
                },
                onClearMessages = { authViewModel.clearMessages() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
