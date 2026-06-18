package com.locket.clone.ui.widget

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LocketMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val imageUrl = message.data["imageUrl"]
        val senderName = message.data["senderName"] ?: "Friend"

        if (imageUrl != null) {
            val inputData = Data.Builder()
                .putString("imageUrl", imageUrl)
                .putString("senderName", senderName)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LocketWidgetWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }
    }
}
