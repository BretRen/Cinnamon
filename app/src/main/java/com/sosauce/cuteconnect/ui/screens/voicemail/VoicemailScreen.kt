package com.sosauce.cuteconnect.ui.screens.voicemail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.sosauce.cuteconnect.data.managers.AudioManager
import com.sosauce.cuteconnect.domain.model.CuteVoicemail
import com.sosauce.cuteconnect.ui.screens.messages.components.PlayPauseButton
import com.sosauce.cuteconnect.ui.shared_components.AnimatedSlider
import com.sosauce.cuteconnect.ui.shared_components.CuteDropdownMenuItem
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.ui.shared_components.MiniCuteSearchbar
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.getContactPfpUri
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import com.sosauce.cuteconnect.utils.rememberSearchbarMaxFloatValue
import com.sosauce.cuteconnect.utils.rememberSearchbarRightPadding
import com.sosauce.cuteconnect.utils.showCuteSearchbar
import com.sosauce.cuteconnect.utils.toReadableDuration
import com.sosauce.cuteconnect.utils.toReadableTime

@Composable
fun VoicemailScreen(
    voicemails: List<CuteVoicemail>,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var query by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        AudioManager.initializePlayer(context.applicationContext)
        onDispose {
            AudioManager.releasePlayer()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                state = listState
            ) {
                items(
                    items = voicemails,
                    key = { it.id }
                ) { voicemail ->
                    VoicemailItem(voicemail)

                }
            }
        }

        AnimatedVisibility(
            visible = listState.showCuteSearchbar,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .navigationBarsPadding()
                .align(rememberSearchbarAlignment())
                .fillMaxWidth(rememberSearchbarMaxFloatValue())
                .padding(end = rememberSearchbarRightPadding())
                .imePadding()
        ) {
            MiniCuteSearchbar(
                query = query,
                onQueryChange = { query = it },
                onNavigateUp = onNavigateUp
            )
        }

    }

}