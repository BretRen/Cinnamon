package com.sosauce.cuteconnect.ui.navigation

import android.telephony.PhoneNumberUtils
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sosauce.cuteconnect.domain.states.CallUiState
import com.sosauce.cuteconnect.ui.screens.contacts.ContactDetails
import com.sosauce.cuteconnect.ui.screens.contacts.ContactsScreen
import com.sosauce.cuteconnect.ui.screens.messages.ConversationScreen
import com.sosauce.cuteconnect.ui.screens.messages.MessagesScreen
import com.sosauce.cuteconnect.ui.screens.phone.CallScreen
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.viewModels.CallViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.screens.debug.DebugMmsScreen
import com.sosauce.cuteconnect.viewModels.ConversationViewModel
import com.sosauce.cuteconnect.utils.NAVIGATION_PREFIX
import com.sosauce.cuteconnect.domain.model.CuteMms
import com.sosauce.cuteconnect.ui.screens.contacts.AboutMeScreen
import com.sosauce.cuteconnect.ui.screens.dialer.DialerScreen
import com.sosauce.cuteconnect.ui.screens.dialer.DialpadScreen
import com.sosauce.cuteconnect.ui.screens.messages.StartConversation
import com.sosauce.cuteconnect.ui.screens.setup.SetupScreen
import com.sosauce.cuteconnect.ui.screens.voicemail.VoicemailScreen
import com.sosauce.cuteconnect.ui.screens.wallpaper.ConversationTheming
import com.sosauce.cuteconnect.utils.CurrentScreen
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.copyMutate
import com.sosauce.cuteconnect.utils.hasBothRoles
import com.sosauce.cuteconnect.viewModels.CommonViewModel
import java.util.Locale
import kotlin.system.measureTimeMillis


@Composable
fun Nav() {

    val backStack = rememberNavBackStack(Screen.Messages).apply {
        CurrentScreen.screen = lastOrNull() ?: Screen.Messages
    }
    val viewModel = koinViewModel<CommonViewModel>()
    val callViewModel = koinViewModel<CallViewModel>()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val conversations by viewModel.pinnedConversations.collectAsStateWithLifecycle()
    val simCards by viewModel.simCards.collectAsStateWithLifecycle()


    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Screen.Contacts> {
                ContactsScreen(
                    contacts = contacts,
                    onNavigate = backStack::add
                )
            }

            entry<Screen.ContactDetails> { key ->
                contacts.find { it.id == key.id }?.let {
                    ContactDetails(
                        contact = it,
                        onNavigateBack = backStack::removeLastOrNull,
                        onNavigate = backStack::add,
                        onHandleCallAction = callViewModel::onHandleCallAction
                    )
                }
            }
            entry<Screen.Dialer> {
                val callLogs by viewModel.callLog.collectAsStateWithLifecycle()

                DialerScreen(
                    onNavigate = backStack::add,
                    callLogs = callLogs,
                    onHandleCallActions = callViewModel::onHandleCallAction,
                    onHandleCommonAction = viewModel::onHandleCommonAction
                )
            }

            entry<Screen.Voicemail> {
                val voicemails by viewModel.voicemails.collectAsStateWithLifecycle()

                VoicemailScreen(
                    voicemails = voicemails,
                    onNavigateUp = backStack::removeLastOrNull
                )
            }

            entry<Screen.Messages> {
                MessagesScreen(
                    conversations = conversations,
                    cuteContacts = contacts,
                    onNavigate = backStack::add,
                    onEditPinnedConvos = { }
                )
            }

            entry<Screen.Conversation>(
                metadata = NavDisplay.transitionSpec { slideInHorizontally { it } togetherWith slideOutHorizontally { it } }
            ) { key ->
                val conversation = conversations.fastFirst { it.threadId == key.threadId }

                ConversationScreen(
                    cuteMessages = messages.fastFilter { it.threadId == key.threadId },
                    cuteConversation = conversation,
                    cuteSimCards = simCards,
                    threadId = key.threadId,
                    onNavigateUp = backStack::removeLastOrNull,
                    onHandleCallAction = callViewModel::onHandleCallAction,
                    onNavigate = backStack::add,
                    onHandleCommonAction = viewModel::onHandleCommonAction
                )
            }

            entry<Screen.ConversationTheming> { key ->
                ConversationTheming(
                    threadId = key.threadId,
                    onPopBackStack = backStack::removeLastOrNull
                )
            }

            entry<Screen.Dialpad> {
                DialpadScreen(
                    contacts = contacts,
                    onNavigate = backStack::add,
                    onHandleCallAction = callViewModel::onHandleCallAction
                )
            }

            entry<Screen.StartConversation> {
                StartConversation(
                    contacts = contacts,
                    onNavigateUp = backStack::removeLastOrNull,
                    onNavigate = backStack::add
                )
            }

            entry<Screen.AboutMe> {
                AboutMeScreen(
                    onNavigateBack = backStack::removeLastOrNull
                )
            }
        }
    )
}