@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.shared_components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.fastForEachIndexed
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.utils.LocalScreen
import com.sosauce.cuteconnect.utils.rememberInteractionSource

@Composable
fun ScreenSelection(
    onNavigate: (Screen) -> Unit,
    dismiss: () -> Unit
) {

    val interactionsSources = List(4) { rememberInteractionSource() }
    val currentScreen = LocalScreen.current
    val screens = listOf(
        ScreenCategory(
            screen = Screen.Messages,
            onClick = { onNavigate(Screen.Messages) },
            unselectedIcon = R.drawable.message_rounded,
            selectedIcon = R.drawable.messages_filled
        ),
        ScreenCategory(
            screen = Screen.Contacts,
            onClick = { onNavigate(Screen.Contacts) },
            unselectedIcon = R.drawable.contacts,
            selectedIcon = R.drawable.contacts_filled
        ),
        ScreenCategory(
            screen = Screen.Dialer,
            onClick = { onNavigate(Screen.Dialer) },
            unselectedIcon = R.drawable.phone,
            selectedIcon = R.drawable.phone_filled
        )
    )


    ButtonGroup(
        modifier = Modifier.fillMaxWidth()
    ) {
        screens.fastForEachIndexed { index, category ->
            ToggleButton(
                checked = currentScreen == category.screen,
                onCheckedChange = {
                    category.onClick()
                    dismiss()
                },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        screens.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                interactionSource = interactionsSources[index],
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionsSources[index])
            ) {
                val icon =
                    if (currentScreen == category.screen) category.selectedIcon else category.unselectedIcon

                Icon(
                    painter = painterResource(icon),
                    contentDescription = null
                )
            }
        }
    }
}

private data class ScreenCategory(
    val screen: Screen,
    val onClick: () -> Unit,
    @param:DrawableRes val unselectedIcon: Int,
    @param:DrawableRes val selectedIcon: Int
)