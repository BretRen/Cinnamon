package com.sosauce.cuteconnect.ui.screens.dialer.components

import com.sosauce.cuteconnect.domain.model.CuteCallLog

data class CallLogsUiState(
    val callLogs: List<CuteCallLog> = emptyList()
)