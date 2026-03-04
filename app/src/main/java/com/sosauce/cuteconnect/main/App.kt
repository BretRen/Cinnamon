@file:OptIn(KoinExperimentalAPI::class)

package com.sosauce.cuteconnect.main

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.sosauce.cuteconnect.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

class App : Application(), KoinStartup, SingletonImageLoader.Factory {


    override fun onCreate() {
        super.onCreate()
    }


    override fun onKoinStartup() = koinConfiguration {
        androidContext(this@App)
        modules(appModule)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory() )
            }
            .crossfade(true)
            .build()
    }


    private fun scheduleMessage() {}
}