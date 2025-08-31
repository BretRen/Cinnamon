package com.sosauce.cuteconnect.ui.shared_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.utils.CurrentScreen

@Composable
fun ScreenSelection(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    val items = listOf(
        NavigationItem(
            title = R.string.messages,
            navigateTo = Screen.Messages,
            icon = R.drawable.message_rounded
        ),
        NavigationItem(
            title = R.string.contacts,
            navigateTo = Screen.Contacts,
            icon = R.drawable.contacts
        ),
        NavigationItem(
            title = R.string.dialer,
            navigateTo = Screen.Dialer,
            icon = R.drawable.call
        )
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp)
    ) {
        items.fastForEach { item ->

            val bgColor by animateColorAsState(
                targetValue = if (item.navigateTo == CurrentScreen.screen) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
            )

            DropdownMenuItem(
                onClick = { onNavigate(item.navigateTo) },
                text = { CuteText(stringResource(item.title)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.title)
                    )
                },
                modifier = Modifier
                    .padding(5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = bgColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Immutable
data class NavigationItem(
    val title: Int,
    val navigateTo: Screen,
    val icon: Int
)