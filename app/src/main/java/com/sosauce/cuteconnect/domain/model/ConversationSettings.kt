package com.sosauce.cuteconnect.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ConversationSettings(
    @PrimaryKey(autoGenerate = false)
    val convoId: Long = 0,
    val draft: String = "",
    val wallpaper: String = "", // Wallpaper's Uri as a String
    val wallpaperBlurIntensity: Int = 0,
    val allWallpapers: List<String> = emptyList(),
    val color: Int = -1,
    val allColors: List<Int> = emptyList()
)