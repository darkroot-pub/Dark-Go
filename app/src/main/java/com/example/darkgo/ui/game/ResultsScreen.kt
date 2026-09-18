package com.example.darkgo.ui.game

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.data.models.WinnerInfo
import com.example.darkgo.ui.components.BingoCardGrid
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBingoGold
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSuccess
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

@Composable
fun ResultsScreen(
    winners: List<WinnerInfo>,
    totalCalledNumbers: Int,
    calledNumbersList: List<Int>,
    playerCard: List<Int>,
    completedRowsCount: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Trophy and Bingo Banner
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DarkGoBingoGold.copy(alpha = 0.2f))
                .border(2.dp, DarkGoBingoGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = DarkGoBingoGold,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "BINGO!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                fontSize = 36.sp
            ),
            color = DarkGoBingoGold
        )

        Text(
            text = if (winners.size > 1) "Multiple Simultaneous Winners!" else "Game Concluded",
            style = MaterialTheme.typography.titleMedium,
            color = DarkGoTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Winners Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBingoGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 WINNER" + if (winners.size > 1) "S" else "",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGoBingoGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                winners.forEach { winner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = winner.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DarkGoTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(DarkGoSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "5 Lines Complete",
                                color = DarkGoSuccess,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Called", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                    Text(
                        "$totalCalledNumbers",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkGoPrimary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Completed", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                    Text(
                        "$completedRowsCount / 5 Rows",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (completedRowsCount == 5) DarkGoSuccess else DarkGoTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final Card Preview
        Text(
            text = "Your Final Board",
            style = MaterialTheme.typography.titleMedium,
            color = DarkGoTextPrimary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        BingoCardGrid(
            cells = playerCard,
            calledNumbers = calledNumbersList.toSet(),
            completedRowIndices = (0..4).filter { row ->
                val start = row * 5
                (0..4).all { col -> calledNumbersList.contains(playerCard[start + col]) }
            },
            isInteractive = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("results_home_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
                Spacer(modifier = Modifier.size(6.dp))
                Text("Home")
            }

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("results_play_again_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Replay, contentDescription = "Play Again", tint = DarkGoBackground)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Play Again", color = DarkGoBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
