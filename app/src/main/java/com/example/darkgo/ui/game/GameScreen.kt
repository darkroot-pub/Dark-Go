package com.example.darkgo.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.ui.audio.SoundAndHapticManager
import com.example.darkgo.ui.components.BingoCardGrid
import com.example.darkgo.ui.components.NumberSelector
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBingoGold
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoError
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSuccess
import com.example.ui.theme.DarkGoSurface
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

data class PlayerStatusUi(
    val name: String,
    val isCurrentTurn: Boolean,
    val isConnected: Boolean,
    val completedRows: Int = 0,
    val completedLines: Int = completedRows
)

@Composable
fun GameScreen(
    roomCodeOrTitle: String,
    isConnected: Boolean,
    isMyTurn: Boolean,
    currentTurnPlayerName: String,
    currentCalledNumber: Int?,
    calledNumbers: List<Int>,
    playerCard: List<Int>,
    completedRowIndices: List<Int>,
    playersStatus: List<PlayerStatusUi>,
    soundManager: SoundAndHapticManager,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    completedLinesCount: Int = completedRowIndices.size,
    onCallNumber: (Int) -> Unit,
    onExitGame: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Game?", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit the match?", color = DarkGoTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onExitGame()
                    }
                ) {
                    Text("Exit", color = DarkGoError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = DarkGoTextSecondary)
                }
            },
            containerColor = DarkGoSurfaceVariant
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGoSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Code & Connection State
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = roomCodeOrTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkGoPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = if (isConnected) "Connected" else "Disconnected",
                        tint = if (isConnected) DarkGoSuccess else DarkGoError,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Completed straight lines pill badge
                val linesCount = if (completedLinesCount > 0) completedLinesCount else completedRowIndices.size
                Box(
                    modifier = Modifier
                        .background(
                            if (linesCount >= 5) DarkGoBingoGold.copy(alpha = 0.2f) else DarkGoSurfaceElevated,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (linesCount >= 5) DarkGoBingoGold else DarkGoBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (linesCount >= 5) "BINGO! 5 Lines" else "$linesCount / 5 Lines",
                        color = if (linesCount >= 5) DarkGoBingoGold else DarkGoTextPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Exit Button
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.testTag("exit_game_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Game", tint = DarkGoTextSecondary)
                }
            }
        },
        containerColor = DarkGoBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Turn Indicator Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMyTurn) DarkGoPrimary.copy(alpha = 0.15f) else DarkGoSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMyTurn) DarkGoPrimary else DarkGoBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isMyTurn) "YOUR TURN!" else "$currentTurnPlayerName's Turn",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isMyTurn) DarkGoPrimary else DarkGoTextPrimary
                        )
                        Text(
                            text = if (isMyTurn) "Choose an uncalled number below" else "Waiting for number call...",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGoTextSecondary
                        )
                    }

                    // Current Number called display badge
                    if (currentCalledNumber != null) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkGoPrimary)
                                .border(1.5.dp, DarkGoBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$currentCalledNumber",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = DarkGoBackground
                            )
                        }
                    }
                }
            }

            // Error display if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage,
                    color = DarkGoError,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5x5 Bingo Board (Auto marks called numbers and highlights completed rows)
            BingoCardGrid(
                cells = playerCard,
                calledNumbers = calledNumbers.toSet(),
                completedRowIndices = completedRowIndices,
                isInteractive = false,
                highlightCurrentNumber = currentCalledNumber
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Called Numbers History Ribbon
            if (calledNumbers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Called: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkGoTextSecondary
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        calledNumbers.takeLast(12).reversed().forEach { num ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkGoSurfaceElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$num",
                                    color = if (num == currentCalledNumber) DarkGoPrimary else DarkGoTextPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (num == currentCalledNumber) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Players progress bar
            if (playersStatus.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    playersStatus.forEach { p ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (p.isCurrentTurn) DarkGoPrimary.copy(alpha = 0.12f) else DarkGoSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (p.isCurrentTurn) DarkGoPrimary.copy(alpha = 0.5f) else DarkGoBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.name,
                                color = if (p.isCurrentTurn) DarkGoPrimary else DarkGoTextPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val pLines = if (p.completedLines > 0) p.completedLines else p.completedRows
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$pLines/5 Lines",
                                color = if (pLines >= 5) DarkGoBingoGold else DarkGoTextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Number Selector for 1..25
            Text(
                text = if (isMyTurn) "Select a number (1–25) to call:" else "Number Keypad (Waiting):",
                style = MaterialTheme.typography.labelMedium,
                color = if (isMyTurn) DarkGoPrimary else DarkGoTextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isProcessing) {
                CircularProgressIndicator(color = DarkGoPrimary, modifier = Modifier.size(32.dp))
            } else {
                NumberSelector(
                    calledNumbers = calledNumbers.toSet(),
                    isMyTurn = isMyTurn,
                    onSelectNumber = { num ->
                        soundManager.playNumberCall()
                        onCallNumber(num)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
