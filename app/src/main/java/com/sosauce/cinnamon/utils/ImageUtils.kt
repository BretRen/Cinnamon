package com.sosauce.cinnamon.utils

import android.content.Context
import android.net.Uri
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis

object ImageUtils {
    fun videoFrameRequester(
        video: Uri?,
        context: Context
    ): ImageRequest {


        val request = ImageRequest.Builder(context)
            .data(video)
            .videoFrameMillis(1000)
            .size(512) // Let's not fetch a 4K thumbnail for a 4k video, not optimal !
            .crossfade(true)
            .build()

        return request
    }
}