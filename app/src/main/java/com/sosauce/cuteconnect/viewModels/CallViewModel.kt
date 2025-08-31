package com.sosauce.cuteconnect.viewModels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.Call
import android.telecom.CallEndpoint
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.activities.CallActivity
import com.sosauce.cuteconnect.data.actions.CallAction
import com.sosauce.cuteconnect.data.managers.CallManager
import com.sosauce.cuteconnect.domain.states.CallState
import com.sosauce.cuteconnect.domain.states.CallUiState
import com.sosauce.cuteconnect.utils.AudioTargetDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallViewModel(
    private val application: Application
): AndroidViewModel(application) {


    private val telecomManager = application.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    private val _callUIState = MutableStateFlow(CallUiState())
    val callUiState = _callUIState.asStateFlow()


    init {
        viewModelScope.launch {
            CallManager.callUiState.collectLatest { state ->
                _callUIState.update { state }
            }
        }
    }

    fun answerCall() = CallManager.answerCall()
    fun declineCall() = CallManager.declineCall()
    fun hangUp() = CallManager.hangupOngoingCall()


    fun shouldMute(mute: Boolean) {
        CallManager.shouldMute(mute)
    }

    fun switchAudioTarget(
        context: Context,
        endpoint: Int
    ) {
        CallManager.switchEndpoint(context, endpoint)
    }

    fun startTone(char: Char) {
        CallManager.startTone(char)
    }

    fun holdOrUnhold() = CallManager.holdOrUnhold()

    fun onHandleCallAction(action: CallAction) {
        when(action) {
            is CallAction.LaunchCall -> {
                if (ActivityCompat.checkSelfPermission(
                        application,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val numberAsUri = "tel:${action.number}".toUri()

                    if (!telecomManager.isInCall) {
                        telecomManager.placeCall(numberAsUri, null)
                        val intent = Intent(application, CallActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        application.startActivity(intent)
                    }

                } else {
                    Toast.makeText(application, "Call permission is needed to create a call.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}