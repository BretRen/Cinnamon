@file:OptIn(ExperimentalFoundationApi::class)

package com.sosauce.cinnamon.presentation.screens.contacts

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.presentation.shared_components.DefaultContactIcon
import com.sosauce.cinnamon.presentation.shared_components.animations.AnimatedSelectedIcon
import com.sosauce.cinnamon.presentation.shared_components.items.CuteListItem
import com.sosauce.cinnamon.utils.SharedTransitionKeys
import com.sosauce.cinnamon.utils.beautifyNumber

@Composable
fun SharedTransitionScope.ContactListItem(
    modifier: Modifier = Modifier,
    contact: CuteContact,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    showNumber: Boolean = false
) {


    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f
    )

    CuteListItem(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        onClick = onClick,
        onLongClick = onLongClick,
        leadingContent = {
            AnimatedSelectedIcon(
                isSelected = isSelected
            ) {
                DefaultContactIcon(
                    firstLetter = contact.displayName.firstOrNull(),
                    contactPfp = contact.photo
                )
            }
        }
    ) {
        Text(
            text = contact.displayName,
            maxLines = 1,
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(SharedTransitionKeys.CONTACT_NAME + contact.id),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                )
                .basicMarquee()
        )
        if (showNumber) {
            Text(
                text = buildString {
                    append(contact.details.phoneNumbers.first().number.beautifyNumber())
                    if (contact.details.phoneNumbers.size > 1) {
                        append(" and ${contact.details.phoneNumbers.size - 1} more")
                    }
                },
                maxLines = 1,
                modifier = Modifier
                    .basicMarquee(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}