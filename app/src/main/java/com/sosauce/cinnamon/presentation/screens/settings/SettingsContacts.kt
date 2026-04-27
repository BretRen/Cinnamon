package com.sosauce.cinnamon.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SettingsContacts() {

    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {}
}