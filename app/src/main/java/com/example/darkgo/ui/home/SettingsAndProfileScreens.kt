package com.example.darkgo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.data.models.UserProfile
import com.example.darkgo.ui.audio.SoundAndHapticManager
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
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

data class AvatarPreset(
    val id: String,
    val name: String,
    val color: Color
)

val PRESET_AVATARS = listOf(
    AvatarPreset("avatar_cyan", "Cyber Cyan", DarkGoPrimary),
    AvatarPreset("avatar_violet", "Void Violet", DarkGoSecondary),
    AvatarPreset("avatar_gold", "Gold Spark", DarkGoBingoGold),
    AvatarPreset("avatar_emerald", "Emerald Blade", DarkGoSuccess),
    AvatarPreset("avatar_crimson", "Crimson Core", DarkGoError)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    soundManager: SoundAndHapticManager,
    onBack: () -> Unit
) {
    val soundEnabled by soundManager.soundEnabled.collectAsState()
    val vibrationEnabled by soundManager.vibrationEnabled.collectAsState()
    var showHowToPlay by remember { mutableStateOf(false) }
    var showFirebaseSetupDialog by remember { mutableStateOf(false) }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    if (showFirebaseSetupDialog) {
        FirebaseSetupInstructionsDialog(onDismiss = { showFirebaseSetupDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("AUDIO & HAPTICS", style = MaterialTheme.typography.labelLarge, color = DarkGoTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = DarkGoPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sound Effects", style = MaterialTheme.typography.titleMedium, color = DarkGoTextPrimary)
                                Text("Number calls & victory audio", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundManager.setSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkGoPrimary,
                                checkedTrackColor = DarkGoPrimary.copy(alpha = 0.5f),
                                uncheckedThumbColor = DarkGoTextDisabled,
                                uncheckedTrackColor = DarkGoSurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = DarkGoPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Haptic Feedback", style = MaterialTheme.typography.titleMedium, color = DarkGoTextPrimary)
                                Text("Vibrate on taps and completed lines", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                            }
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { soundManager.setVibrationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkGoPrimary,
                                checkedTrackColor = DarkGoPrimary.copy(alpha = 0.5f),
                                uncheckedThumbColor = DarkGoTextDisabled,
                                uncheckedTrackColor = DarkGoSurfaceElevated
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("GAMEPLAY RULES", style = MaterialTheme.typography.labelLarge, color = DarkGoTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                onClick = { showHowToPlay = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DarkGoPrimary)
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Column {
                        Text("How to Play Dark Go", style = MaterialTheme.typography.titleMedium, color = DarkGoTextPrimary)
                        Text("5x5 layout, straight lines & win rules", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("DEVELOPER & BACKEND", style = MaterialTheme.typography.labelLarge, color = DarkGoTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                onClick = { showFirebaseSetupDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DarkGoSecondary)
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Column {
                        Text("Firebase Setup Instructions", style = MaterialTheme.typography.titleMedium, color = DarkGoTextPrimary)
                        Text("Connected to darkgo-72baf Realtime DB", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dark Go Version 1.0 (Native Kotlin / Compose)",
                style = MaterialTheme.typography.bodySmall,
                color = DarkGoTextDisabled,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onSaveProfile: (displayName: String, bio: String, avatarId: String) -> Unit = { _, _, _ -> },
    onClearMessages: () -> Unit = {},
    onBack: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: "") }
    var editBio by remember(userProfile) { mutableStateOf(userProfile?.bio ?: "") }
    var selectedAvatarId by remember(userProfile) { mutableStateOf(userProfile?.avatarId ?: "avatar_cyan") }

    val currentAvatar = PRESET_AVATARS.find { it.id == selectedAvatarId } ?: PRESET_AVATARS[0]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Profile" else "Profile", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                isEditing = false
                                onClearMessages()
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGoTextPrimary)
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(
                            onClick = {
                                isEditing = true
                                editName = userProfile?.displayName ?: ""
                                editBio = userProfile?.bio ?: ""
                                selectedAvatarId = userProfile?.avatarId ?: "avatar_cyan"
                                onClearMessages()
                            },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = DarkGoPrimary)
                        }
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Feedback messages
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkGoError.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoError)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(errorMessage, color = DarkGoError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = onClearMessages, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DarkGoError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = successMessage != null) {
                if (successMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkGoSuccess.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoSuccess)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(successMessage, color = DarkGoSuccess, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = onClearMessages, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DarkGoSuccess, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Big Avatar Display
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(currentAvatar.color.copy(alpha = 0.2f))
                    .border(3.dp, currentAvatar.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (userProfile?.displayName ?: "P").take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp
                    ),
                    color = currentAvatar.color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                // EDIT MODE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CHOOSE AVATAR THEME",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGoTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PRESET_AVATARS.forEach { avatar ->
                                val isSelected = avatar.id == selectedAvatarId
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(avatar.color.copy(alpha = if (isSelected) 0.35f else 0.15f))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) avatar.color else DarkGoBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedAvatarId = avatar.id }
                                        .testTag("avatar_select_${avatar.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = avatar.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(avatar.color)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Name Input
                        Text(
                            text = "DISPLAY NAME",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGoTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { if (it.length <= 25) editName = it },
                            placeholder = { Text("Enter your player name", color = DarkGoTextDisabled) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_display_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGoPrimary,
                                unfocusedBorderColor = DarkGoBorder,
                                focusedTextColor = DarkGoTextPrimary,
                                unfocusedTextColor = DarkGoTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bio / Tagline Input
                        Text(
                            text = "STATUS / BIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGoTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { if (it.length <= 100) editBio = it },
                            placeholder = { Text("e.g. Bingo master & straight line tactician", color = DarkGoTextDisabled) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_bio_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGoPrimary,
                                unfocusedBorderColor = DarkGoBorder,
                                focusedTextColor = DarkGoTextPrimary,
                                unfocusedTextColor = DarkGoTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save & Cancel Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
                    ) {
                        Text("Cancel", color = DarkGoTextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSaveProfile(editName, editBio, selectedAvatarId)
                            isEditing = false
                        },
                        enabled = editName.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = DarkGoBackground, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = DarkGoBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", color = DarkGoBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // VIEW MODE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = userProfile?.displayName ?: "Player",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = DarkGoTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = userProfile?.email ?: "Offline Account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGoTextSecondary
                        )

                        if (!userProfile?.bio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkGoSurfaceElevated, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "\"${userProfile?.bio}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkGoPrimary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "UID: ${userProfile?.uid ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGoTextDisabled
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Game Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CAREER STATISTICS", style = MaterialTheme.typography.labelMedium, color = DarkGoTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${userProfile?.gamesPlayed ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkGoTextPrimary
                                )
                                Text("Played", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${userProfile?.gamesWon ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkGoBingoGold
                                )
                                Text("Won", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val rate = if ((userProfile?.gamesPlayed ?: 0) > 0) {
                                    ((userProfile!!.gamesWon.toFloat() / userProfile.gamesPlayed) * 100).toInt()
                                } else 0
                                Text(
                                    text = "$rate%",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkGoSuccess
                                )
                                Text("Win Rate", style = MaterialTheme.typography.bodySmall, color = DarkGoTextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isEditing = true
                        editName = userProfile?.displayName ?: ""
                        editBio = userProfile?.bio ?: ""
                        selectedAvatarId = userProfile?.avatarId ?: "avatar_cyan"
                        onClearMessages()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_edit_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = DarkGoBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", color = DarkGoBackground, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Play Dark Go", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("1. 5×5 Board Layout", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("Each card contains exactly 25 cells with numbers 1 through 25 appearing once.", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("2. Custom Sequential Card Creation", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("Tap empty cells in order: your 1st tap assigns 1, 2nd assigns 2, up to 25th tap for 25. Number assignment is based on tap order, not cell position.", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("3. Turn-Based Calling & Auto-Marking", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("On your turn, choose an uncalled number from 1–25. When called, that number is marked on EVERY player's card automatically.", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("4. All Straight Lines Count as Bingo!", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("Complete ANY 5 STRAIGHT LINES to achieve BINGO! Straight lines include all 5 horizontal rows, all 5 vertical columns, and both diagonals (12 total straight lines on the 5×5 board). Complete any 5 lines to trigger BINGO!", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("5. Multiple Winners", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("If one number call causes multiple players to achieve their 5th straight line simultaneously, all qualifying players share the victory!", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = DarkGoPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkGoSurfaceVariant
    )
}

@Composable
fun FirebaseSetupInstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Firebase Integration Guide", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Current Project:", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("Project ID: darkgo-72baf\nDatabase URL: https://darkgo-72baf-default-rtdb.firebaseio.com\nPackage: com.darkroot.darkgo", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text("Checklist for Full Online Play:", fontWeight = FontWeight.Bold, color = DarkGoPrimary)
                Text("1. Firebase Console -> Authentication -> Enable Email/Password & Google Sign-In providers.\n2. Firebase Console -> Realtime Database -> Create Database and apply database.rules.json.\n3. Verify SHA-1 fingerprint for Google Sign-In.\n4. google-services.json is packaged directly at /app/google-services.json.", color = DarkGoTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DarkGoPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkGoSurfaceVariant
    )
}
