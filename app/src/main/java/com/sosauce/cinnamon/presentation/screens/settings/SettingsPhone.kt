package com.sosauce.cinnamon.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.datastore.rememberGroupSubsequentCalls
import com.sosauce.cinnamon.presentation.screens.settings.components.SettingsWithTitle
import com.sosauce.cinnamon.presentation.screens.settings.components.SwitchSettingsCards

@Composable
fun SettingsPhone() {

    val scrollState = rememberScrollState()
    var groupSubCalls by rememberGroupSubsequentCalls()



    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {

        SettingsWithTitle(title = R.string.phone) {
            SwitchSettingsCards(
                checked = groupSubCalls,
                onCheckedChange = { groupSubCalls = !groupSubCalls },
                topDp = 24.dp,
                bottomDp = 24.dp,
                text = "Group subsequent calls"
            )
        }
    }
}