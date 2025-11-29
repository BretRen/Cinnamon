package com.sosauce.cuteconnect.data.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import android.telecom.Connection
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.telephony.TelephonyCallback
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.activities.CallActivity
import com.sosauce.cuteconnect.data.receivers.CallReceiver
import com.sosauce.cuteconnect.data.services.CallService
import com.sosauce.cuteconnect.domain.model.AudioRoute
import com.sosauce.cuteconnect.domain.states.CallState
import com.sosauce.cuteconnect.main.MainActivity
import com.sosauce.cuteconnect.ui.screens.phone.CallingState
import com.sosauce.cuteconnect.utils.ACCEPT_INCOMING_CALL
import com.sosauce.cuteconnect.utils.AUDIO_SOURCE
import com.sosauce.cuteconnect.utils.AudioTargetDevice
import com.sosauce.cuteconnect.utils.DECLINE_INCOMING_CALL
import com.sosauce.cuteconnect.utils.FULL_SCREEN_INTENT
import com.sosauce.cuteconnect.utils.HANGUP_ONGOING_CALL
import com.sosauce.cuteconnect.utils.MUTE_SOURCE
import com.sosauce.cuteconnect.utils.SWITCH_AUDIO_SOURCE
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

// Inspired by Fossify's call manager!

/**
 * A bridge between an InCallService (CallService) and the ViewModel.
 * It's easier having it as an object as InCallService has a hard time with DI
 */
class CallManager(
    private val context: Context
) {

    private var callServiceCallback: CallServiceCallback? = null
    private var androidCallCallback : AndroidCallCallback? = null

    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager


    private val _callingState = MutableStateFlow(CallingState())
    val callingState = _callingState.asStateFlow()

    fun registerCallServiceCallback(cb: CallServiceCallback) {
        callServiceCallback = cb
    }

    fun registerAndroidCallCallback(cb: AndroidCallCallback) {
        androidCallCallback = cb
    }

    fun unregisterCallServiceCallback() {
        callServiceCallback = null
    }

    fun unregisterAndroidCallCallback() {
        androidCallCallback = null
    }

    fun answerCall() = androidCallCallback?.answerCall()

    fun declineCall() = androidCallCallback?.declineCall()

    fun startCall(number: Uri) = telecomManager.placeCall(number, null)


    fun hangupOngoingCall() = androidCallCallback?.hangupOngoingCall()

    fun toggleMute(mute: Boolean) = callServiceCallback?.toggleMute(mute)

    fun startTone(char: Char) = androidCallCallback?.startTone(char)

    fun toggleHold() = androidCallCallback?.toggleHold()

    fun switchAudioRoute(route: AudioRoute) = callServiceCallback?.switchAudioRoute(route)

    fun updateAvailableAudioRoutes(routes: List<AudioRoute>) {
        _callingState.update {
            it.copy(availableAudioRoutes = routes)
        }
    }

    fun updateCurrentAudioRoute(route: AudioRoute) {
        _callingState.update {
            it.copy(currentAudioRoute = route)
        }
    }

    fun updateIsMuted(isMuted: Boolean) {
        _callingState.update {
            it.copy(isMuted = isMuted)
        }
    }

    fun updateIsHolding(isHolding: Boolean) {
        _callingState.update {
            it.copy(isHolding = isHolding)
        }
    }

    fun updateCallState(callState: CallState) {
        _callingState.update {
            it.copy(callState = callState)
        }
    }

    fun updateTimeSpent(time: Long) {
        _callingState.update {
            it.copy(timeSpentInCall = time)
        }
    }

    fun updateNumber(number: String) {
        _callingState.update {
            it.copy(number = number)
        }
    }
}


interface AndroidCallCallback {
    fun answerCall()
    fun declineCall()
    fun hangupOngoingCall()
    fun startTone(char: Char)
    fun toggleHold()
}
interface CallServiceCallback {
    fun toggleMute(mute: Boolean)
    fun switchAudioRoute(route: AudioRoute)
}