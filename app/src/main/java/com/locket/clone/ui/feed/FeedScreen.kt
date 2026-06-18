package com.locket.clone.ui.feed

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locket.clone.data.model.LocketPhoto
import com.locket.clone.data.repository.PhotoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUserId: String,
    photoRepository: PhotoRepository = remember { PhotoRepository() },
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var feedPhotos by remember { mutableStateOf<List<LocketPhoto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshFeed() {
        coroutineScope.launch {
            isLoading = true
            feedPhotos = photoRepository.getFeed(currentUserId)
            isLoading = false
        }
    }

    LaunchedEffect(currentUserId) {
        refreshFeed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locket History", color = Color.White, fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9F0A))
            }
        } else if (feedPhotos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No live photos yet. Start sending some!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(feedPhotos) { photo ->
                    FeedPhotoCard(
                        photo = photo,
                        currentUserId = currentUserId,
                        onReact = { emoji ->
                            coroutineScope.launch {
                                val success = photoRepository.addReaction(photo.id, currentUserId, emoji)
                                if (success) {
                                    Toast.makeText(context, "Reacted $emoji", Toast.LENGTH_SHORT).show()
                                    refreshFeed()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FeedPhotoCard(
    photo: LocketPhoto,
    currentUserId: String,
    onReact: (String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    val formattedTime = remember(photo.timestamp) { dateFormatter.format(Date(photo.timestamp)) }
    
    var showReactionsPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(photo.senderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(formattedTime, color = Color.Gray, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = "Locket Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (photo.reactions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        photo.reactions.forEach { (_, emoji) ->
                            Text(emoji, fontSize = 14.sp)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (photo.caption.isNotEmpty()) {
                    Text(
                        text = photo.caption,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showReactionsPicker = !showReactionsPicker },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF9F0A))
                    ) {
                        Text("React ⚡")
                    }
                }

                if (showReactionsPicker) {
                    val emojis = listOf("❤️", "🔥", "😂", "😮", "😢", "👍")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        emojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        onReact(emoji)
                                        showReactionsPicker = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
