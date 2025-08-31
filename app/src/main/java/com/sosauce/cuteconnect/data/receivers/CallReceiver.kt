package com.sosauce.cuteconnect.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sosauce.cuteconnect.data.managers.CallManager
import com.sosauce.cuteconnect.utils.ACCEPT_INCOMING_CALL
import com.sosauce.cuteconnect.utils.DECLINE_INCOMING_CALL
import com.sosauce.cuteconnect.utils.HANGUP_ONGOING_CALL
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CallReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        when (intent?.action) {
            ACCEPT_INCOMING_CALL -> CallManager.answerCall()
            DECLINE_INCOMING_CALL -> CallManager.declineCall()
            HANGUP_ONGOING_CALL -> CallManager.hangupOngoingCall()
        }
    }
}


