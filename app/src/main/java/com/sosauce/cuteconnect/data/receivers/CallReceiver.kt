package com.sosauce.cuteconnect.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sosauce.cuteconnect.data.managers.CallManager
import com.sosauce.cuteconnect.utils.ACCEPT_INCOMING_CALL
import com.sosauce.cuteconnect.utils.DECLINE_INCOMING_CALL
import com.sosauce.cuteconnect.utils.HANGUP_ONGOING_CALL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CallReceiver: BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent?) {

        val callManager by inject<CallManager>()

        when (intent?.action) {
            ACCEPT_INCOMING_CALL -> callManager.answerCall()
            DECLINE_INCOMING_CALL -> callManager.declineCall()
            HANGUP_ONGOING_CALL -> callManager.hangupOngoingCall()
        }
    }
}


