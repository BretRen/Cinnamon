package com.sosauce.cinnamon.di

import androidx.room.Room
import androidx.work.WorkManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import com.sosauce.cinnamon.data.contact_settings.ContactSettingsDao
import com.sosauce.cinnamon.data.contact_settings.ContactSettingsDatabase
import com.sosauce.cinnamon.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cinnamon.data.conversation_settings.ConversationSettingsDatabase
import com.sosauce.cinnamon.data.datastore.UserPreferences
import com.sosauce.cinnamon.data.managers.CallManager
import com.sosauce.cinnamon.data.managers.CallNotificationManager
import com.sosauce.cinnamon.data.managers.MessageNotificationManager
import com.sosauce.cinnamon.data.schedulers.scheduled_messages.ScheduledMessagesDao
import com.sosauce.cinnamon.data.schedulers.scheduled_messages.ScheduledMessagesDatabase
import com.sosauce.cinnamon.data.telephony.CuteTelephonyManager
import com.sosauce.cinnamon.domain.repository.ArchivedThreadsRepository
import com.sosauce.cinnamon.domain.repository.BlockedNumbersManager
import com.sosauce.cinnamon.domain.repository.ContactDetailsRepository
import com.sosauce.cinnamon.domain.repository.ContactsRepository
import com.sosauce.cinnamon.domain.repository.ConversationsRepository
import com.sosauce.cinnamon.domain.repository.DialerRepository
import com.sosauce.cinnamon.domain.repository.MessagesRepository
import com.sosauce.cinnamon.domain.repository.SimsRepository
import com.sosauce.cinnamon.domain.repository.VoicemailsRepository
import com.sosauce.cinnamon.ui.screens.archived.ArchivedViewModel
import com.sosauce.cinnamon.ui.screens.contacts.ContactDetailsViewModel
import com.sosauce.cinnamon.ui.screens.contacts.ContactsViewModel
import com.sosauce.cinnamon.ui.screens.dialer.DialerViewModel
import com.sosauce.cinnamon.ui.screens.dialer.DialpadViewModel
import com.sosauce.cinnamon.ui.screens.messages.ConversationDetailsViewModel
import com.sosauce.cinnamon.ui.screens.messages.MessagesViewModel
import com.sosauce.cinnamon.ui.screens.messages.components.bottombar.BottomBarViewModel
import com.sosauce.cinnamon.ui.screens.phone.CallingViewModel
import com.sosauce.cinnamon.ui.screens.settings.MigrationViewModel
import com.sosauce.cinnamon.ui.screens.voicemail.VoicemailViewModel
import com.sosauce.cinnamon.ui.screens.wallpaper.ThemingViewModel
import com.sosauce.cinnamon.ui.shared_components.SimsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single<ConversationSettingsDao> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ConversationSettingsDatabase::class.java,
            name = "conversationSettings.db"
        ).build().dao
    }

    single<ScheduledMessagesDao> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ScheduledMessagesDatabase::class.java,
            name = "scheduledMessages.db"
        ).build().dao
    }

    single<ContactSettingsDao> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ContactSettingsDatabase::class.java,
            name = "contactSettings.db"
        ).build().dao
    }

    single { CoroutineScope(SupervisorJob()) }
    single { PhoneNumberOfflineGeocoder.getInstance(androidContext()) }
    single { PhoneNumberUtil.getInstance(androidContext()) }
    single { WorkManager.getInstance(androidContext()) }

    singleOf(::UserPreferences)
    singleOf(::BlockedNumbersManager)
    singleOf(::CallManager)
    singleOf(::MessageNotificationManager)
    singleOf(::CallNotificationManager)
    singleOf(::CuteTelephonyManager)
    singleOf(::ConversationsRepository)
    singleOf(::SimsRepository)
    singleOf(::ContactsRepository)
    singleOf(::ContactDetailsRepository)
    singleOf(::MessagesRepository)
    singleOf(::ArchivedThreadsRepository)
    singleOf(::DialerRepository)
    singleOf(::VoicemailsRepository)


    viewModelOf(::ContactsViewModel)
    viewModelOf(::ContactDetailsViewModel)
    viewModelOf(::ConversationDetailsViewModel)
    viewModelOf(::ThemingViewModel)
    viewModelOf(::MessagesViewModel)
    viewModelOf(::ArchivedViewModel)
    viewModelOf(::DialerViewModel)
    viewModelOf(::VoicemailViewModel)
    viewModelOf(::DialpadViewModel)
    viewModelOf(::CallingViewModel)
    viewModelOf(::BottomBarViewModel)
    viewModelOf(::SimsViewModel)
    viewModelOf(::MigrationViewModel)
}