package com.sosauce.cuteconnect.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDatabase
import com.sosauce.cuteconnect.data.managers.CallManager
import com.sosauce.cuteconnect.data.managers.CallNotificationManager
import com.sosauce.cuteconnect.domain.repository.CommonRepository
import com.sosauce.cuteconnect.viewModels.CallViewModel
import com.sosauce.cuteconnect.viewModels.CommonViewModel
import com.sosauce.cuteconnect.viewModels.ConversationViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf

val appModule = module {

    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ConversationSettingsDatabase::class.java,
            name = "conversationSettings.db"
        ).build().dao
    }

    singleOf(::CallNotificationManager)
    singleOf(::CommonRepository)

    viewModelOf(::CommonViewModel)
    viewModelOf(::CallViewModel)
    viewModelOf(::ConversationViewModel)
}