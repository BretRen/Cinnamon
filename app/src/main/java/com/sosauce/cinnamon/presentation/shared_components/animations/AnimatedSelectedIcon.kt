package com.sosauce.cinnamon.presentation.shared_components.animations

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.sosauce.cinnamon.presentation.shared_components.DefaultContactIcon
import com.sosauce.cinnamon.presentation.shared_components.DefaultGroupChatIcon
import com.sosauce.cinnamon.presentation.shared_components.SelectedItemLogo
import com.sosauce.cinnamon.utils.SharedTransitionKeys
import com.sosauce.cinnamon.utils.bouncySpec
import com.sosauce.cinnamon.utils.getContactPfpFromNumber

@Composable
fun AnimatedSelectedIcon(
    isSelected: Boolean,
    unselectedContent: @Composable (AnimatedContentScope.() -> Unit)
) {
    AnimatedContent(
        targetState = isSelected,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = scaleIn(bouncySpec()),
                initialContentExit = scaleOut(bouncySpec()),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        modifier = Modifier.padding(start = 10.dp)
    ) {
        if (it) {
            SelectedItemLogo()
        } else {
            unselectedContent()
        }
    }
}