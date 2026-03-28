package com.sosauce.cinnamon.ui.shared_components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun BottomActionButtons(
    content: @Composable () -> Unit
) {
    SmallFloatingActionButton(
        onClick = {},
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        content = content
    )
}