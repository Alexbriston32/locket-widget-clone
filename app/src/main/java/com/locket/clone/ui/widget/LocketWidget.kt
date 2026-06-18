package com.locket.clone.ui.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.locket.clone.ui.camera.CameraActivity

class LocketWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            LocketWidgetContent()
        }
    }

    @Composable
    private fun LocketWidgetContent() {
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val senderName = prefs[LocketWidgetWorker.KEY_SENDER_NAME] ?: "No photos yet"
        val imagePath = prefs[LocketWidgetWorker.KEY_IMAGE_PATH]

        val bitmap = remember(imagePath) {
            if (imagePath != null) {
                try {
                    BitmapFactory.decodeFile(imagePath)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.BLACK))
                .clickable(actionStartActivity<CameraActivity>()),
            contentAlignment = Alignment.BottomStart
        ) {
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = "Latest Locket Photo",
                    modifier = GlanceModifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📸",
                        style = TextStyle(
                            fontSize = androidx.glance.text.FontSize(24f)
                        )
                    )
                    Text(
                        text = "Tap to take a photo!",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.WHITE),
                            fontSize = androidx.glance.text.FontSize(12f)
                        ),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(android.graphics.Color.argb(120, 0, 0, 0)))
                    .padding(8.dp)
            ) {
                Text(
                    text = if (bitmap != null) "From: $senderName" else senderName,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.WHITE),
                        fontSize = androidx.glance.text.FontSize(11f)
                    )
                )
            }
        }
    }
}
