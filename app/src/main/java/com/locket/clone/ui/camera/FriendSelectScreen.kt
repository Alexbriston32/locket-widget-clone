package com.locket.clone.ui.camera

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.locket.clone.data.repository.PhotoRepository
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSelectScreen(
    currentUserId: String,
    photoFile: File,
    caption: String,
    friendRepository: FriendRepository = remember { FriendRepository() },
    photoRepository: PhotoRepository = remember { PhotoRepository() },
    onUploadSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var friendsList by remember { mutableStateOf<List<User>>(emptyList()) }
    val selectedFriendIds = remember { mutableStateListOf<String>() }
    var isAllSelected by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        friendsList = friendRepository.getFriends(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Locket", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Text(
                text = "Who gets this photo?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .clickable {
                        isAllSelected = !isAllSelected
                        selectedFriendIds.clear()
                        if (isAllSelected) {
                            selectedFriendIds.addAll(friendsList.map { it.uid })
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = {
                        isAllSelected = it
                        selectedFriendIds.clear()
                        if (isAllSelected) {
                            selectedFriendIds.addAll(friendsList.map { it.uid })
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF9F0A))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("All Friends 🌟", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(friendsList) { friend ->
                    val isChecked = selectedFriendIds.contains(friend.uid)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .clickable {
                                if (isChecked) {
                                    selectedFriendIds.remove(friend.uid)
                                    isAllSelected = false
                                } else {
                                    selectedFriendIds.add(friend.uid)
                                    if (selectedFriendIds.size == friendsList.size) {
                                        isAllSelected = true
                                    }
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (it) {
                                    selectedFriendIds.add(friend.uid)
                                    if (selectedFriendIds.size == friendsList.size) {
                                        isAllSelected = true
                                    }
                                } else {
                                    selectedFriendIds.remove(friend.uid)
                                    isAllSelected = false
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF9F0A))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(friend.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("@${friend.username}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedFriendIds.isEmpty()) {
                        Toast.makeText(context, "Select at least one friend!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUploading = true
                    coroutineScope.launch {
                        val senderName = "My Name"
                        val senderProfile = ""
                        
                        val result = photoRepository.uploadPhoto(
                            senderId = currentUserId,
                            senderName = senderName,
                            senderProfilePic = senderProfile,
                            localFile = photoFile,
                            caption = caption,
                            recipients = selectedFriendIds.toList()
                        )
                        
                        isUploading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "Locket sent successfully! 🚀", Toast.LENGTH_SHORT).show()
                            onUploadSuccess()
                        } else {
                            Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isUploading && selectedFriendIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send to Home Screens 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
