package com.example.darkgo.ui.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.data.models.Player
import com.example.darkgo.data.models.Room
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBingoGold
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoError
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSecondary
import com.example.ui.theme.DarkGoSuccess
import com.example.ui.theme.DarkGoSurface
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTertiary
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    room: Room,
    currentUid: String,
    isProcessing: Boolean,
    errorMessage: String?,
    onCreateCardClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onLeaveRoomClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isHost = room.hostUid == currentUid
    val playersList = room.players.values.toList().sortedBy { it.joinedAt }
    val currentPlayer = room.players[currentUid]
    val isReady = currentPlayer?.ready == true

    val allReady = playersList.size >= 2 && playersList.all { it.ready }

    BackHandler(onBack = onLeaveRoomClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Lobby", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onLeaveRoomClick, modifier = Modifier.testTag("leave_lobby_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave", tint = DarkGoTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGoSurface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkGoBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Room Code Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ROOM CODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkGoTextSecondary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = room.roomCode,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 6.sp
                            ),
                            color = DarkGoPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Dark Go Room Code", room.roomCode)
                                clipboard.setPrimaryClip(clip)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Room code copied: ${room.roomCode}")
                                }
                            },
                            modifier = Modifier.testTag("copy_room_code_button")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = DarkGoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${playersList.size} / ${room.maxPlayers} Players Joined",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Players List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYERS",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGoTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHost) "Min 2 players to start" else "Waiting for host",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGoTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Players list items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playersList) { player ->
                    PlayerLobbyItem(
                        player = player,
                        isHost = player.uid == room.hostUid,
                        isMe = player.uid == currentUid
                    )
                }
            }

            // Error Display if any
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = DarkGoError,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User card action: If not ready -> create/submit card button
            if (!isReady) {
                Button(
                    onClick = onCreateCardClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("setup_card_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGoSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = "Create Card")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create & Confirm 5x5 Card", fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = DarkGoSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your card is confirmed and ready!",
                            color = DarkGoSuccess,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Host controls / status
            if (isHost) {
                Button(
                    onClick = onStartGameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_game_button"),
                    enabled = allReady && !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = DarkGoBackground, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Game", tint = DarkGoBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (playersList.size < 2) "Need At Least 2 Players" else if (!allReady) "Waiting for Players..." else "Start Match",
                            color = DarkGoBackground,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "Waiting for the host to start the match...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGoTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PlayerLobbyItem(player: Player, isHost: Boolean, isMe: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = if (isMe) androidx.compose.foundation.BorderStroke(1.dp, DarkGoPrimary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isHost) DarkGoBingoGold.copy(alpha = 0.2f) else DarkGoSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHost) {
                        Icon(Icons.Default.Star, contentDescription = "Host", tint = DarkGoBingoGold, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = player.displayName.take(1).uppercase(),
                            color = DarkGoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.displayName + if (isMe) " (You)" else "",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DarkGoTextPrimary
                        )
                        if (isHost) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HOST",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkGoBingoGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (player.connected) "Connected" else "Reconnecting...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (player.connected) DarkGoTextSecondary else DarkGoTertiary
                    )
                }
            }

            // Readiness Badge
            if (player.ready) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = DarkGoSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ready", color = DarkGoSuccess, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Waiting", tint = DarkGoTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Waiting", color = DarkGoTertiary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
