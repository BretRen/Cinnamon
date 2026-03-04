package com.sosauce.cuteconnect.ui.screens.settings

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.navigation.SettingsScreens
import com.sosauce.cuteconnect.ui.screens.settings.components.AboutCard
import com.sosauce.cuteconnect.ui.screens.settings.components.SettingsCategoryCard
import com.sosauce.cuteconnect.ui.shared_components.buttons.CuteNavigationButton

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit
) {
    val backStack = rememberNavBackStack(SettingsScreens.Settings)
    val scrollState = rememberScrollState()
    val items = listOf(
        Item(
            icon = R.drawable.palette,
            name = stringResource(R.string.look_and_feel),
            description = stringResource(R.string.look_and_feel_desc),
            onNavigate = { backStack.add(SettingsScreens.LookAndFeel) }
        ),
        Item(
            icon = R.drawable.behavior,
            name = stringResource(R.string.behavior),
            description = stringResource(R.string.behavior_desc),
            onNavigate = { backStack.add(SettingsScreens.Behavior) }
        ),
        Item(
            icon = R.drawable.migrate,
            name = stringResource(R.string.migration),
            description = stringResource(R.string.migration_desc),
            onNavigate = { backStack.add(SettingsScreens.Migration) }
        )
    )


    NavDisplay(
        backStack = backStack,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        onBack = {
            if (backStack.size == 1) {
                onNavigateUp()
            } else { backStack.removeLastOrNull() }
        },
        predictivePopTransitionSpec = {
            ContentTransform(
                fadeIn(),
                slideOutHorizontally { it },
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<SettingsScreens.Settings> {
                Scaffold(
                    bottomBar = { CuteNavigationButton(onNavigateUp = onNavigateUp) }
                ) { pv ->
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(pv)
                    ) {
                        AboutCard()
                        Spacer(Modifier.height(20.dp))
                        items.fastForEachIndexed { index, item ->
                            SettingsCategoryCard(
                                icon = item.icon,
                                name = item.name,
                                description = item.description,
                                topDp = if (index == 0) 24.dp else 4.dp,
                                bottomDp = if (index == items.lastIndex) 24.dp else 4.dp,
                                onNavigate = item.onNavigate
                            )
                        }
                    }
                }
            }


            entry<SettingsScreens.LookAndFeel> {
                SettingsLookAndFeel(
                    onNavigateUp = backStack::removeLastOrNull
                )
            }

            entry<SettingsScreens.Behavior> {
                SettingsBehavior(
                    onNavigateUp = backStack::removeLastOrNull
                )
            }

            entry<SettingsScreens.Migration> {
                SettingsMigration(
                    onNavigateUp = backStack::removeLastOrNull
                )
            }
        }
    )
}

private data class Item(
    val name: String,
    val description: String,
    val icon: Int,
    val onNavigate: () -> Unit
)