package com.sosauce.cuteconnect.domain.states

import android.os.Build
import android.os.Parcelable
import android.telecom.CallEndpoint
import androidx.annotation.RequiresApi

data class CallUiState(
    val callState: CallState = CallState.DIALING,
    val number: String = "",
    val isMuted: Boolean = false,
    val isHolding: Boolean = false,
    val timeSpentInCall: Int = 0,
    val availableEndpoints: List<CallEndpoint?> = emptyList(),
    val currentEndpoint: Int = CompatCallEndpoint.TYPE_EARPIECE
)


enum class CallState{
   RINGING, // When we're receiving a call
   DIALING, // When we're calling
   ONGOING,
   ENDED
}

/**
 * A compatibility layer to CallEndpoint to easily manage audio route below and above Android 14.
 * Maybe we don't need it but I can't test on A14+ so for now we just do.
 */
object CompatCallEndpoint {

    val TYPE_EARPIECE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        CallEndpoint.TYPE_EARPIECE
    } else {
        1
    }

    val TYPE_BLUETOOTH = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        CallEndpoint.TYPE_BLUETOOTH
    } else {
        2
    }

    val TYPE_SPEAKER = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        CallEndpoint.TYPE_SPEAKER
    } else {
        4
    }

    // This is different from bluetooth, bluetooth could be any device, I know this is obvious my my ass would forget
    val TYPE_HEADSET = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        CallEndpoint.TYPE_WIRED_HEADSET
    } else {
        3
    }

    val TYPE_UNKNOWN = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        CallEndpoint.TYPE_UNKNOWN
    } else {
        -1
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun CallEndpoint.toCompat(): Int {
        return when (endpointType) {
            CallEndpoint.TYPE_EARPIECE -> TYPE_EARPIECE
            CallEndpoint.TYPE_BLUETOOTH -> TYPE_BLUETOOTH
            CallEndpoint.TYPE_SPEAKER -> TYPE_SPEAKER
            CallEndpoint.TYPE_UNKNOWN -> TYPE_UNKNOWN
            else -> TYPE_UNKNOWN
        }
    }
}