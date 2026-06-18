package com.locket.clone.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.locket.clone.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean = currentUser != null

    suspend fun getProfile(uid: String = currentUser?.uid ?: ""): User? {
        if (uid.isEmpty()) return null
        return try {
            firestore.collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProfile(user: User): Boolean {
        val uid = currentUser?.uid ?: return false
        val userWithUid = user.copy(uid = uid)
        return try {
            firestore.collection("users").document(uid).set(userWithUid).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        auth.signOut()
    }
}
