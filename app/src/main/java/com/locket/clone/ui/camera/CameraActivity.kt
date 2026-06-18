package com.locket.clone.ui.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.locket.clone.data.repository.AuthRepository
import com.locket.clone.ui.auth.AuthScreen
import com.locket.clone.ui.feed.FeedScreen
import com.locket.clone.ui.friends.FriendScreen
import com.locket.clone.ui.theme.LocketCloneTheme
import java.io.File

class CameraActivity : ComponentActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocketCloneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocketAppNavigation(authRepository)
                }
            }
        }
    }
}

@Composable
fun LocketAppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isLoggedIn()) "camera" else "auth"

    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }
    var capturedCaption by remember { mutableStateOf("") }
    val currentUserId = authRepository.currentUser?.uid ?: "mock_user_id"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(
                authRepository = authRepository,
                onAuthSuccess = {
                    navController.navigate("camera") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("camera") {
            CameraScreen(
                onPhotoCaptured = { file, caption ->
                    capturedPhotoFile = file
                    capturedCaption = caption
                    navController.navigate("friend_select")
                },
                onNavigateToFeed = {
                    navController.navigate("feed")
                },
                onNavigateToFriends = {
                    navController.navigate("friends")
                }
            )
        }

        composable("friend_select") {
            val file = capturedPhotoFile
            if (file != null) {
                FriendSelectScreen(
                    currentUserId = currentUserId,
                    photoFile = file,
                    caption = capturedCaption,
                    onUploadSuccess = {
                        capturedPhotoFile = null
                        capturedCaption = ""
                        navController.navigate("camera") {
                            popUpTo("camera") { inclusive = false }
                        }
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            } else {
                navController.navigateUp()
            }
        }

        composable("friends") {
            FriendScreen(
                currentUserId = currentUserId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable("feed") {
            FeedScreen(
                currentUserId = currentUserId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
