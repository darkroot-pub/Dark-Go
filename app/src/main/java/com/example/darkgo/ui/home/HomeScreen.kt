package com.example.darkgo.ui.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.data.models.UserProfile
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoError
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSecondary
import com.example.ui.theme.DarkGoSuccess
import com.example.ui.theme.DarkGoSurface
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTertiary
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    onPlayOnline: () -> Unit,
    onPractice: () -> Unit,
    onVsBots: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of your Dark Go account?", color = DarkGoTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = DarkGoError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = DarkGoTextSecondary)
                }
            },
            containerColor = DarkGoSurfaceVariant
        )
    }

    Scaffold(containerColor = DarkGoBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DarkGoPrimary.copy(alpha = 0.2f))
                            .border(1.5.dp, DarkGoPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.displayName ?: "P").take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DarkGoPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userProfile?.displayName ?: "Player",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkGoTextPrimary
                        )
                        Text(
                            text = "Online Bingo 25",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGoTextSecondary
                        )
                    }
                }

                Row {
                    IconButton(onClick = onProfile, modifier = Modifier.testTag("home_profile_button")) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = DarkGoTextSecondary)
                    }
                    IconButton(onClick = onSettings, modifier = Modifier.testTag("home_settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = DarkGoTextSecondary)
                    }
                    IconButton(onClick = { showLogoutDialog = true }, modifier = Modifier.testTag("home_logout_button")) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = DarkGoError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DARK GO",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp
                        ),
                        color = DarkGoPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "5×5 Horizontal Row Bingo",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Menu Options
            HomeMenuOption(
                title = "Play Online",
                subtitle = "Multiplayer with up to 10 players via room code or lobby",
                icon = Icons.Default.Public,
                accentColor = DarkGoPrimary,
                testTag = "menu_play_online",
                onClick = onPlayOnline
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuOption(
                title = "Practice Mode",
                subtitle = "Offline single player to create custom cards & test rows",
                icon = Icons.Default.Casino,
                accentColor = DarkGoSecondary,
                testTag = "menu_practice",
                onClick = onPractice
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuOption(
                title = "Vs Bots",
                subtitle = "Challenge 1 to 4 smart AI opponents with turn cycles",
                icon = Icons.Default.SmartToy,
                accentColor = DarkGoSuccess,
                testTag = "menu_vs_bots",
                onClick = onVsBots
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuOption(
                title = "How to Play",
                subtitle = "Game rules, 5-row winning condition & custom card creation",
                icon = Icons.Default.HelpOutline,
                accentColor = DarkGoTertiary,
                testTag = "menu_how_to_play",
                onClick = onHowToPlay
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HomeMenuOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkGoTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGoTextSecondary
                )
            }
        }
    }
}
