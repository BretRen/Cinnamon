package com.sosauce.cuteconnect.ui.shared_components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomActionButtons(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    SmallFloatingActionButton(
        onClick = {},
        modifier = modifier
            .padding(end = 15.dp),
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        content = content
    )
}