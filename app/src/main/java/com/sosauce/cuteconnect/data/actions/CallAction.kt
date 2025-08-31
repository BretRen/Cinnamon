package com.sosauce.cuteconnect.data.actions

sealed interface CallAction {
    data class LaunchCall(val number: String) : CallAction
}