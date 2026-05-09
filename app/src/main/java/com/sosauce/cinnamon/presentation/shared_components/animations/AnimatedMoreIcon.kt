package com.sosauce.cinnamon.presentation.shared_components.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.sosauce.cinnamon.R

@Composable
fun AnimatedMoreIcon(
    expanded: Boolean
) {
    AnimatedContent(
        targetState = expanded
    ) { isExpanded ->
        val icon = if (!isExpanded) R.drawable.more_vert else R.drawable.close
        Icon(
            painter = painterResource(icon),
            contentDescription = null
        )
    }
}