package com.sosauce.cuteconnect.ui.screens.phone

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.sosauce.cuteconnect.domain.states.CallState
import com.sosauce.cuteconnect.domain.states.CallUiState
import com.sosauce.cuteconnect.domain.states.CompatCallEndpoint
import com.sosauce.cuteconnect.ui.screens.phone.components.Dialpad
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.toReadableTime
import com.sosauce.cuteconnect.viewModels.CallViewModel

@Composable
fun CallScreen(
    callViewModel: CallViewModel,
    callUiState: CallUiState
) {

    val context = LocalContext.current
    val activity = LocalActivity.current!!
    var showDialpad by remember { mutableStateOf(false) }
    val micBgColor by animateColorAsState(
        targetValue = if (callUiState.isMuted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainerHigh
    )
    val micColor by animateColorAsState(
        targetValue = if (callUiState.isMuted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
    )
    val volumeBgColor by animateColorAsState(
        targetValue = if (callUiState.currentEndpoint == CompatCallEndpoint.TYPE_SPEAKER) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainerHigh
    )
    val volumeColor by animateColorAsState(
        targetValue = if (callUiState.currentEndpoint == CompatCallEndpoint.TYPE_SPEAKER) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
    )
    val displayName = remember(callUiState.number) {
        callUiState.number.getContactNameOrNothing(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            DefaultContactIcon(
                firstLetter = displayName.firstOrNull(),
                size = 150.dp,
                fontSize = 50.sp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                letterColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = callUiState.callState,
                transitionSpec = { scaleIn() togetherWith scaleOut() }
            ) {
                CuteText(
                    text = when(it) {
                        CallState.RINGING, CallState.DIALING  -> "Ringing..."
                        CallState.ONGOING -> displayName
                        CallState.ENDED -> "Call ended"
                    },
                    fontSize = 30.sp,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }
            Spacer(Modifier.height(10.dp))
            CuteText(
                text = callUiState.timeSpentInCall.toReadableTime(),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(
                visible = callUiState.isHolding,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                CuteText(
                    text = "On hold",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        callViewModel.shouldMute(!callUiState.isMuted)
                    },
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = micBgColor,
                        contentColor = micColor
                    )
                ) {
                    AnimatedContent(
                        targetState = callUiState.isMuted
                    ) { isMuted ->
                        Icon(
                            imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        if (callUiState.currentEndpoint in arrayOf(CompatCallEndpoint.TYPE_SPEAKER, CompatCallEndpoint.TYPE_EARPIECE)) {
                            val endpoint = if (callUiState.currentEndpoint == CompatCallEndpoint.TYPE_EARPIECE) {
                                CompatCallEndpoint.TYPE_SPEAKER
                            } else CompatCallEndpoint.TYPE_EARPIECE

                            callViewModel.switchAudioTarget(context, endpoint)
                        }
                    },
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = volumeBgColor,
                        contentColor = volumeColor,
                    )
                ) {

                    val animatedEndpoint = when (callUiState.currentEndpoint) {
                        CompatCallEndpoint.TYPE_EARPIECE -> Icons.AutoMirrored.Rounded.VolumeDown
                        CompatCallEndpoint.TYPE_SPEAKER -> Icons.AutoMirrored.Rounded.VolumeUp
                        CompatCallEndpoint.TYPE_HEADSET -> Icons.Rounded.Headset
                        CompatCallEndpoint.TYPE_BLUETOOTH -> Icons.Rounded.BluetoothAudio
                        CompatCallEndpoint.TYPE_UNKNOWN -> Icons.AutoMirrored.Rounded.VolumeDown
                        else -> Icons.AutoMirrored.Rounded.VolumeDown
                    }

                    AnimatedContent(
                        targetState = animatedEndpoint
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = null
                        )
                    }
                }
                Button(
                    onClick = { showDialpad = true },
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dialpad,
                        contentDescription = null
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = callViewModel::holdOrUnhold,
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = null
                    )
                }
                Button(
                    onClick = callViewModel::hangUp,
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        modifier = Modifier.rotate(135f)
                    )
                }
                Button(
                    onClick = {  },
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showDialpad,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .statusBarsPadding(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Dialpad(
                onCloseDialpad = { showDialpad = false },
                onSendTone = callViewModel::startTone
            )
        }
    }
}