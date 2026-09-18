package com.example.darkgo.ui.card

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.ui.audio.SoundAndHapticManager
import com.example.darkgo.ui.components.BingoCardGrid
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoError
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSurface
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreationScreen(
    viewModel: CardCreationViewModel,
    soundManager: SoundAndHapticManager,
    title: String = "Create Your Card",
    subtitle: String = "Tap empty cells to place numbers 1 to 25 sequentially",
    onConfirm: (List<Int>) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGoTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGoSurface)
            )
        },
        containerColor = DarkGoBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Instructions & Progress Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.nextNumberToAssign <= 25) {
                                "Next to place: "
                            } else {
                                "Board Complete! (25/25)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkGoTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (uiState.nextNumberToAssign <= 25) {
                            Box(
                                modifier = Modifier
                                    .background(DarkGoPrimary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.nextNumberToAssign}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = DarkGoBackground
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The 5x5 Board
            BingoCardGrid(
                cells = uiState.cells,
                calledNumbers = emptySet(),
                completedRowIndices = emptyList(),
                isInteractive = !uiState.isConfirmed,
                onCellClick = { index ->
                    soundManager.playTap()
                    viewModel.onCellTapped(index)
                }
            )

            // Error Message display
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = DarkGoError,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Buttons: Undo, Reset, Quick Fill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        soundManager.playTap()
                        viewModel.undo()
                    },
                    modifier = Modifier.weight(1f).testTag("undo_button"),
                    enabled = uiState.history.isNotEmpty() && !uiState.isConfirmed,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Undo")
                }

                OutlinedButton(
                    onClick = {
                        soundManager.playTap()
                        viewModel.reset()
                    },
                    modifier = Modifier.weight(1f).testTag("reset_button"),
                    enabled = uiState.history.isNotEmpty() && !uiState.isConfirmed,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Reset")
                }

                OutlinedButton(
                    onClick = {
                        soundManager.playTap()
                        viewModel.randomFill()
                    },
                    modifier = Modifier.weight(1.2f).testTag("random_fill_button"),
                    enabled = !uiState.isConfirmed,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Casino, contentDescription = "Quick Fill", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Quick Fill")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Card Action Button
            Button(
                onClick = {
                    soundManager.playTap()
                    val validatedCard = viewModel.validateAndConfirm()
                    if (validatedCard != null) {
                        onConfirm(validatedCard)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_card_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.cells.filterNotNull().size == 25
            ) {
                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = DarkGoBackground)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Confirm Card",
                    color = DarkGoBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
