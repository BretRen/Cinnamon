package com.sosauce.cuteconnect.utils

import android.content.Context
import android.net.Uri
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import coil3.transform.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {
    fun imageRequester(
        img: Any?,
        context: Context,
        applyRoundedCorners: Boolean = false
    ): ImageRequest {
        val request = ImageRequest.Builder(context)
            .data(img)
            .crossfade(true)
            .apply {
                if (applyRoundedCorners) {
                    transformations(RoundedCornersTransformation(50f))
                }
            }
            .build()

        return request
    }
}