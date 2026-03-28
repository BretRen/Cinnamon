package com.sosauce.cinnamon.domain.repository

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionManager
import com.sosauce.cinnamon.domain.model.CuteSimCard

class SimsRepository(
    private val context: Context
) {

    private val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)

    @SuppressLint("MissingPermission")
    fun fetchSims(): List<CuteSimCard> {
        val simCards = mutableListOf<CuteSimCard>()

        subscriptionManager.activeSubscriptionInfoList?.forEach { subInfo ->
            simCards.add(
                CuteSimCard(
                    subId = subInfo.subscriptionId,
                    name = subInfo.displayName?.toString() ?: "No name",
                    carrierName = subInfo.carrierName?.toString() ?: "No carrier",
                    color = subInfo.iconTint
                )
            )
        }
        return simCards
    }
}