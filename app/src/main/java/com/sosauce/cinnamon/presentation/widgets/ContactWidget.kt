package com.sosauce.cinnamon.presentation.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.sosauce.cinnamon.R

object ContactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            Box(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Image(
                    provider = ImageProvider(R.drawable.wallpaper_test),
                    contentDescription = null
                )
            }
        }
    }
}