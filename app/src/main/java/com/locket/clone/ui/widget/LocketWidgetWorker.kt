package com.locket.clone.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream

class LocketWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        val KEY_SENDER_NAME = stringPreferencesKey("sender_name")
        val KEY_IMAGE_PATH = stringPreferencesKey("image_path")
    }

    override suspend fun doWork(): Result {
        val imageUrl = inputData.getString("imageUrl") ?: return Result.failure()
        val senderName = inputData.getString("senderName") ?: "Friend"

        return try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val imageResult = imageLoader.execute(request)
            if (imageResult is SuccessResult) {
                val drawable = imageResult.drawable
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    val file = File(context.filesDir, "widget_latest_photo.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(LocketWidget::class.java)
                    
                    for (glanceId in glanceIds) {
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                            val mutablePrefs = prefs.toMutablePreferences()
                            mutablePrefs[KEY_SENDER_NAME] = senderName
                            mutablePrefs[KEY_IMAGE_PATH] = file.absolutePath
                            mutablePrefs
                        }
                        LocketWidget().update(context, glanceId)
                    }
                    return Result.success()
                }
            }
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
