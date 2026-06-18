package com.locket.clone.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.locket.clone.data.model.User
import kotlinx.coroutines.tasks.await

class FriendRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val maxFriends = 20

    suspend fun getFriends(uid: String): List<User> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendFriendRequest(fromUid: String, toUsername: String): Result<Unit> {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("username", toUsername)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("User not found"))
            }

            val targetUser = query.documents.first().toObject(User::class.java)
                ?: return Result.failure(Exception("Error decoding user"))

            if (targetUser.uid == fromUid) {
                return Result.failure(Exception("Cannot add yourself"))
            }

            val senderFriends = getFriends(fromUid)
            if (senderFriends.size >= maxFriends) {
                return Result.failure(Exception("You have reached the limit of $maxFriends close friends"))
            }

            val requestId = "${fromUid}_${targetUser.uid}"
            val requestMap = mapOf(
                "id" to requestId,
                "fromUid" to fromUid,
                "toUid" to targetUser.uid,
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("friend_requests").document(requestId).set(requestMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingRequests(uid: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("friend_requests")
                .whereEqualTo("toUid", uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun acceptFriendRequest(requestId: String, currentUid: String, friendUid: String): Boolean {
        return try {
            val myProfileDoc = firestore.collection("users").document(currentUid).get().await()
            val friendProfileDoc = firestore.collection("users").document(friendUid).get().await()

            val myProfile = myProfileDoc.toObject(User::class.java) ?: return false
            val friendProfile = friendProfileDoc.toObject(User::class.java) ?: return false

            firestore.collection("users").document(currentUid).collection("friends").document(friendUid).set(friendProfile)
            firestore.collection("users").document(friendUid).collection("friends").document(currentUid).set(myProfile)

            firestore.collection("friend_requests").document(requestId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun declineFriendRequest(requestId: String): Boolean {
        return try {
            firestore.collection("friend_requests").document(requestId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
