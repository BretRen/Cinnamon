@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.utils.toTime
import java.sql.Timestamp

@Composable
fun Timestamp(time: Long) {
    Text(
        text = time.toTime(),
        style = MaterialTheme.typography.bodyMediumEmphasized.copy(
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(50)
            )
            .padding(5.dp)
    )
}