@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.datastore.rememberInitialTab
import com.sosauce.cinnamon.presentation.screens.settings.components.DropdownMenuSettingsCard
import com.sosauce.cinnamon.presentation.screens.settings.components.SettingsWithTitle
import com.sosauce.cinnamon.utils.DefaultTabOption
import com.sosauce.cinnamon.utils.getItemShape
import com.sosauce.cinnamon.utils.toLocalizedTab

@Composable
fun SettingsBehavior() {
    val context = LocalContext.current
    var initialTab by rememberInitialTab()
    val allTabs = listOf(
        DefaultTabOption.MESSAGES,
        DefaultTabOption.CONTACTS,
        DefaultTabOption.DIALER,
        DefaultTabOption.DIALPAD
    )

    Column {
        SettingsWithTitle(title = R.string.general) {
            DropdownMenuSettingsCard(
                value = context.toLocalizedTab(initialTab),
                topDp = 24.dp,
                bottomDp = 24.dp,
                text = R.string.default_tab,
                dropdownContent = {
                    allTabs.fastForEachIndexed { index, tab ->

                        val isSelected = tab == initialTab

                        DropdownMenuItem(
                            selected = isSelected,
                            onClick = { initialTab = tab },
                            text = { Text(context.toLocalizedTab(tab)) },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null
                                    )
                                }
                            },
                            shapes = MenuDefaults.itemShape(index, allTabs.size)
                        )
                    }
                }
            )
        }
    }
}