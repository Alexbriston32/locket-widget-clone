package com.locket.clone.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.locket.clone.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository = remember { AuthRepository() },
    onAuthSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showOnboarding by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var createdUid by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!showOnboarding) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Locket Clone 📸",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (!isCodeSent) {
                    Text("Enter your phone number to sign in", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = { Text("Phone Number (e.g. +1234567)") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (phoneNumber.trim().isNotEmpty()) {
                                isLoading = true
                                isCodeSent = true
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A)),
                        enabled = !isLoading
                    ) {
                        Text("Send Verification Code", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            createdUid = "mock_uid_${System.currentTimeMillis()}"
                            showOnboarding = true
                        }
                    ) {
                        Text("Skip / Test Login (Bypass) ⚡", color = Color(0xFFFF9F0A))
                    }
                } else {
                    Text("Enter the 6-digit SMS code", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = smsCode,
                        onValueChange = { smsCode = it },
                        placeholder = { Text("SMS Verification Code") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val loggedInUser = authRepository.currentUser
                                if (loggedInUser != null) {
                                    val profile = authRepository.getProfile(loggedInUser.uid)
                                    if (profile != null) {
                                        onAuthSuccess(loggedInUser.uid)
                                    } else {
                                        createdUid = loggedInUser.uid
                                        showOnboarding = true
                                    }
                                } else {
                                    val mockUid = "mock_uid_${System.currentTimeMillis()}"
                                    createdUid = mockUid
                                    showOnboarding = true
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A)),
                        enabled = !isLoading
                    ) {
                        Text("Verify Code", color = Color.Black)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Profile 👤",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        if (displayName.trim().isNotEmpty() && username.trim().isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                val user = User(
                                    uid = createdUid,
                                    displayName = displayName,
                                    username = username.lowercase().trim(),
                                    phoneNumber = phoneNumber
                                )
                                val success = authRepository.saveProfile(user)
                                isLoading = false
                                onAuthSuccess(createdUid)
                            }
                        } else {
                            Toast.makeText(context, "Fill in all fields!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A)),
                    enabled = !isLoading
                ) {
                    Text("Start Sharing", color = Color.Black)
                }
            }
        }
    }
}
