@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.contacts.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ContactActionsRow(
    contact: CuteContact,
    onNavigate: (Screen) -> Unit,
    onHandleCallAction: (CallAction) -> Unit
) {


    val interactionSources = List(4) { remember { MutableInteractionSource() } }
    val actions = listOf(
        ContactActionsItem(
            icon = R.drawable.call,
            onClick = {}
        ),
        ContactActionsItem(
            icon = R.drawable.messages_filled,
            onClick = {}
        ),
        ContactActionsItem(
            icon = if (contact.isFavorite) R.drawable.favorite_filled else R.drawable.favorite,
            onClick = {},
            tint = if (contact.isFavorite) MaterialTheme.colorScheme.error else null
        )
    )

    ButtonGroup(
        overflowIndicator = {},
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        actions.forEachIndexed { index, item ->
            customItem(
                {
                    FilledTonalIconButton(
                        onClick = { item.onClick() },
                        interactionSource = interactionSources[index],
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                            .animateWidth(interactionSources[index])
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null
                        )
                    }
                },
                {}
            )
        }
    }
}

private data class ContactActionsItem(
    val id: String = Uuid.random().toString(),
    val icon: Int,
    val onClick: () -> Unit,
    val tint: Color? = null
)