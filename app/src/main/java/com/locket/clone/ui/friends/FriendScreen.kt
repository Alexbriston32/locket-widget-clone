package com.locket.clone.ui.friends

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.clone.data.model.User
import com.locket.clone.data.repository.FriendRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    currentUserId: String,
    friendRepository: FriendRepository = remember { FriendRepository() },
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var friendsList by remember { mutableStateOf<List<User>>(emptyList()) }
    var pendingRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var searchUsername by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    fun refreshData() {
        coroutineScope.launch {
            friendsList = friendRepository.getFriends(currentUserId)
            pendingRequests = friendRepository.getPendingRequests(currentUserId)
        }
    }

    LaunchedEffect(currentUserId) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Close Friends", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212)),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("⬅️", fontSize = 20.sp)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Add Friend by Username", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchUsername,
                    onValueChange = { searchUsername = it },
                    placeholder = { Text("username") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (searchUsername.trim().isNotEmpty()) {
                            isSearching = true
                            coroutineScope.launch {
                                val result = friendRepository.sendFriendRequest(currentUserId, searchUsername.trim())
                                isSearching = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                                    searchUsername = ""
                                    refreshData()
                                } else {
                                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = !isSearching,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))
                ) {
                    Text("Add", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pendingRequests.isNotEmpty()) {
                Text("Pending Requests", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(pendingRequests) { request ->
                        val requestId = request["id"] as? String ?: ""
                        val fromUid = request["fromUid"] as? String ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("User: $fromUid", color = Color.White)
                            Row {
                                TextButton(onClick = {
                                    coroutineScope.launch {
                                        val success = friendRepository.acceptFriendRequest(requestId, currentUserId, fromUid)
                                        if (success) {
                                            Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
                                            refreshData()
                                        }
                                    }
                                }) {
                                    Text("Accept", color = Color.Green)
                                }
                                TextButton(onClick = {
                                    coroutineScope.launch {
                                        val success = friendRepository.declineFriendRequest(requestId)
                                        if (success) {
                                            refreshData()
                                        }
                                    }
                                }) {
                                    Text("Decline", color = Color.Red)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Your Close Friends (${friendsList.size}/20)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (friendsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No close friends added yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(friendsList) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👤",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(friend.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("@${friend.username}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
