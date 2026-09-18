package com.example.darkgo.ui.room

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoError
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSecondary
import com.example.ui.theme.DarkGoSurface
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary
import com.example.ui.theme.DarkGoTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrJoinRoomScreen(
    publicRooms: List<Map<String, Any>>,
    isLoading: Boolean,
    errorMessage: String?,
    onCreateRoom: () -> Unit,
    onJoinByCode: (String) -> Unit,
    onJoinPublicRoom: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Join by Code / Public, 1: Create
    var enteredRoomCode by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiplayer Rooms", color = DarkGoTextPrimary, fontWeight = FontWeight.Bold) },
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkGoSurfaceVariant,
                contentColor = DarkGoPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DarkGoPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Join Room", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Create Room", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = DarkGoError,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (selectedTab == 0) {
                // JOIN TAB
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Method 1 — Enter 5-character Room Code",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkGoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = enteredRoomCode,
                            onValueChange = { if (it.length <= 5) enteredRoomCode = it.uppercase() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("room_code_input"),
                            placeholder = { Text("e.g. A7K92", color = DarkGoTextDisabled) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGoPrimary,
                                unfocusedBorderColor = DarkGoBorder,
                                focusedTextColor = DarkGoTextPrimary,
                                unfocusedTextColor = DarkGoTextPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onJoinByCode(enteredRoomCode.trim()) },
                            enabled = enteredRoomCode.trim().length >= 4 && !isLoading,
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("join_by_code_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Join", color = DarkGoBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Method 2 — Public Waiting Rooms (${publicRooms.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkGoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (publicRooms.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = DarkGoTextDisabled,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No public rooms currently waiting.",
                                    color = DarkGoTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Create a new room or share a room code!",
                                    color = DarkGoTextDisabled,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(publicRooms) { roomMap ->
                                val roomId = roomMap["roomId"] as? String ?: ""
                                val code = roomMap["roomCode"] as? String ?: "ROOM"
                                val count = (roomMap["playerCount"] as? Number)?.toInt() ?: 1
                                val max = (roomMap["maxPlayers"] as? Number)?.toInt() ?: 10

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJoinPublicRoom(roomId) },
                                    colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceElevated),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = code,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp
                                                ),
                                                color = DarkGoPrimary
                                            )
                                            Text(
                                                text = "Waiting for players...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DarkGoTextSecondary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = null,
                                                tint = DarkGoTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$count / $max",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DarkGoTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Button(
                                                onClick = { onJoinPublicRoom(roomId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DarkGoSecondary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Join", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // CREATE TAB
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkGoSurfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = DarkGoPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Host a New Dark Go Match",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = DarkGoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "A unique 5-character room code will be generated. Up to 10 players can join your room.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGoTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onCreateRoom,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("create_room_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = DarkGoBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Create Room Now",
                                color = DarkGoBackground,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
