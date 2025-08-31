package com.sosauce.cuteconnect.ui.shared_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.utils.thenIf

@Composable
fun CuteDropdownMenuItem(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    clickEnabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    containerColor: Color? = null
) {
    Row(
        modifier =
            modifier
                .clip(shape)
                .combinedClickable(
                    enabled = clickEnabled,
                    interactionSource = interactionSource,
                    indication = ripple(true),
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
                .thenIf(containerColor != null) {
                    background(containerColor!!)
                }
                .fillMaxWidth()
                // Preferred min and max width used during the intrinsic measurement.
                .sizeIn(
                    minWidth = 112.dp,
                    maxWidth = 280.dp,
                    minHeight = 48.dp
                )
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) colors.leadingIconColor else colors.disabledLeadingIconColor,
        ) {
            Box(Modifier.defaultMinSize(minWidth = 24.dp)) {
                leadingIcon()
            }
        }
        CompositionLocalProvider(LocalContentColor provides if (enabled) colors.textColor else colors.disabledTextColor) {
            Box(
                Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp,
                        end =
                            if (trailingIcon != null) {
                                12.dp
                            } else {
                                0.dp
                            }
                    )
            ) {
                text()
            }
        }
        if (trailingIcon != null) {
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) colors.trailingIconColor else colors.disabledTrailingIconColor
            ) {
                Box(Modifier.defaultMinSize(minWidth = 24.dp)) {
                    trailingIcon()
                }
            }
        }
    }
}
// Band-aid fix, fix cute drop down menu item later
@Composable
fun CuteDropdownMenuItemUnclickable(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    shape: Shape = RoundedCornerShape(12.dp),
    containerColor: Color? = null
) {
    Row(
        modifier =
            modifier
                .clip(shape)
                .thenIf(containerColor != null) {
                    background(containerColor!!)
                }
                .fillMaxWidth()
                // Preferred min and max width used during the intrinsic measurement.
                .sizeIn(
                    minWidth = 112.dp,
                    maxWidth = 280.dp,
                    minHeight = 48.dp
                )
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) colors.leadingIconColor else colors.disabledLeadingIconColor,
        ) {
            Box(Modifier.defaultMinSize(minWidth = 24.dp)) {
                leadingIcon()
            }
        }
        CompositionLocalProvider(LocalContentColor provides if (enabled) colors.textColor else colors.disabledTextColor) {
            Box(
                Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp,
                        end =
                            if (trailingIcon != null) {
                                12.dp
                            } else {
                                0.dp
                            }
                    )
            ) {
                text()
            }
        }
        if (trailingIcon != null) {
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) colors.trailingIconColor else colors.disabledTrailingIconColor
            ) {
                Box(Modifier.defaultMinSize(minWidth = 24.dp)) {
                    trailingIcon()
                }
            }
        }
    }
}