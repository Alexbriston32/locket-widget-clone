package com.locket.clone.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.locket.clone.data.model.LocketPhoto
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class PhotoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadPhoto(
        senderId: String,
        senderName: String,
        senderProfilePic: String,
        localFile: File,
        caption: String,
        recipients: List<String>
    ): Result<LocketPhoto> {
        return try {
            val photoId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("photos/$photoId.jpg")
            
            val fileUri = Uri.fromFile(localFile)
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val photo = LocketPhoto(
                id = photoId,
                senderId = senderId,
                senderName = senderName,
                senderProfilePicture = senderProfilePic,
                imageUrl = downloadUrl,
                caption = caption,
                timestamp = System.currentTimeMillis(),
                recipients = recipients,
                reactions = emptyMap()
            )

            firestore.collection("photos").document(photoId).set(photo).await()
            Result.success(photo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeed(userId: String): List<LocketPhoto> {
        return try {
            val query = firestore.collection("photos")
                .whereArrayContains("recipients", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            query.toObjects(LocketPhoto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addReaction(photoId: String, userId: String, emoji: String): Boolean {
        return try {
            val photoRef = firestore.collection("photos").document(photoId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(photoRef)
                val photo = snapshot.toObject(LocketPhoto::class.java)
                if (photo != null) {
                    val updatedReactions = photo.reactions.toMutableMap()
                    updatedReactions[userId] = emoji
                    transaction.update(photoRef, "reactions", updatedReactions)
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
