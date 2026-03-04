@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components.bubble

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.ui.screens.messages.ConversationActions
import com.sosauce.cuteconnect.ui.screens.messages.components.bottombar.dashedBorder
import com.sosauce.cuteconnect.ui.shared_components.AnimatedSlider
import com.sosauce.cuteconnect.ui.theme.CuteConnectTheme
import com.sosauce.cuteconnect.utils.ImageUtils
import com.sosauce.cuteconnect.utils.getMMSSize
import com.sosauce.cuteconnect.utils.isAudio
import com.sosauce.cuteconnect.utils.isEmoji
import com.sosauce.cuteconnect.utils.isImage
import com.sosauce.cuteconnect.utils.isLink
import com.sosauce.cuteconnect.utils.isVideo
import com.sosauce.cuteconnect.utils.rememberInteractionSource
import com.sosauce.cuteconnect.utils.thenIf
import com.sosauce.cuteconnect.utils.toTime
import com.sosauce.sweetselect.SweetSelectState
import com.sosauce.sweetselect.sweetClickable
import kotlin.time.DurationUnit
import kotlin.times

@UnstableApi
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    cuteMessage: CuteMessage,
    isSelected: Boolean = false,
    sandwichPosition: SandwichPosition = SandwichPosition.SOLO,
    onHandleConversationActions: (ConversationActions) -> Unit,
    sweetSelectState: SweetSelectState<CuteMessage>
) {

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val bubbleColor = when {
        cuteMessage.body.isEmoji() || cuteMessage.isScheduled -> Color.Transparent
        cuteMessage.type == Telephony.Sms.MESSAGE_TYPE_SENT -> MaterialTheme.colorScheme.primaryFixedDim
        else -> MaterialTheme.colorScheme.tertiaryFixedDim
    }
    var isTimestampVisible by remember { mutableStateOf(false) }
    val alignment = remember {
        if (cuteMessage.type == Telephony.Sms.MESSAGE_TYPE_INBOX) {
            Alignment.Start
        } else Alignment.End
    }
    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest

    // Row that takes the whole width that allows selection, revealing timestamp etc... NOT the actual content or bubble
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sweetClickable(
                item = cuteMessage,
                state = sweetSelectState,
                onClick = { isTimestampVisible = !isTimestampVisible }
            )
            .thenIf(isSelected) {
                Modifier.background(surfaceHighest)
            }
    ) {
        // Content/bubble skeleton! Provides alignment, max width etc… SHOULD NOT BE STYLED
        Box(
            modifier = Modifier
                .padding(
                    top = if (sandwichPosition == SandwichPosition.TOP || sandwichPosition == SandwichPosition.SOLO) 10.dp else 1.dp,
                    bottom = if (sandwichPosition == SandwichPosition.BOTTOM || sandwichPosition == SandwichPosition.SOLO) 10.dp else 1.dp,
                    start = 10.dp,
                    end = 10.dp
                )
                .align(alignment)
                .widthIn(max = configuration.screenWidthDp.dp * 0.8f)

        ) {
            if (!cuteMessage.isMms) {
                TextBubbleContent(
                    text = cuteMessage.body,
                    type = cuteMessage.type,
                    bubbleColor = bubbleColor,
                    sandwichPosition = sandwichPosition,
                    isScheduled = cuteMessage.isScheduled
                )
            } else {

                Column(
                    horizontalAlignment = alignment
                ) {
                    val attachment = cuteMessage.attachment ?: return
                    CompositionLocalProvider(LocalContentColor provides contentColorFor(bubbleColor)) {
                        attachment.attachmentDetails.fastForEach { details ->
                            when {
                                details.uri.isImage(context) -> {
                                    ImageAttachment(image = details.uri)
                                }
                                details.uri.isVideo(context) -> {
                                    VideoAttachment(video = details.uri)
                                }
                                details.uri.isAudio(context) -> {
                                    AudioAttachment(
                                        audio = details.uri,
                                        modifier = modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(bubbleColor)
                                    )
                                }
                                else -> {
                                    val fileSize = remember { context.getMMSSize(details.uri) }



                                    Box(
                                        modifier = Modifier
                                            .clip(BubbleShape(sandwichPosition, cuteMessage.type))
                                            .background(bubbleColor)
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(details.uri, context.contentResolver.getType(details.uri))
                                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                }
                                                context.startActivity(intent)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.file),
                                                contentDescription = null
                                            )
                                            Column {
                                                Text(
                                                    text = details.filename,
                                                    maxLines = 1,
                                                    modifier = Modifier.basicMarquee()
                                                )
                                                Text(
                                                    text = Formatter.formatFileSize(
                                                        context,
                                                        fileSize
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (attachment.body.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        TextBubbleContent(
                            text = attachment.body,
                            type = cuteMessage.type,
                            bubbleColor = bubbleColor,
                            sandwichPosition = sandwichPosition,
                            isScheduled = cuteMessage.isScheduled
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = isTimestampVisible,
            modifier = Modifier.align(alignment)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cuteMessage.delivered) {
                    Icon(
                        painter = painterResource(R.drawable.delivered),
                        contentDescription = null
                    )
                }
                Timestamp(cuteMessage.date)
            }
        }
        when(cuteMessage.type) {
            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(alignment)
                ) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Sending...")
                }
            }
            Telephony.Sms.MESSAGE_TYPE_FAILED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(alignment)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Not sent.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .combinedClickable(
//                onClick = {
//                    if (isInSelectMode) {
//                        onAddMessageToSelected()
//                    } else {
//                        isTimestampVisible = !isTimestampVisible
//                    }
//                },
//                onLongClick = onAddMessageToSelected
//            )
//            .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
//    ) {
//
//        cuteMessage.attachment?.attachmentDetails?.let {
//
//            it.fastForEachIndexed { index, details ->
//
//                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.contentColorFor(bubbleColor)) {
//
//                    when {
//                        details.uri.isImage(context) -> {
//                            ImageAttachment(
//                                image = details.uri,
//                                modifier = Modifier
//                                    .padding(
//                                        vertical = 2.dp,
//                                        horizontal = 10.dp,
//                                    )
//                                    .widthIn(max = configuration.screenWidthDp.dp * 0.7f)
//                                    .align(alignment)
//                            )
//                        }
//                        details.uri.isAudio(context) -> {
//                            AudioAttachment(
//                                audio = details.uri,
//                                message = cuteMessage,
//                                sliderColors = SliderDefaults.colors(
//                                    activeTrackColor = contentColorFor(bubbleColor),
//                                    inactiveTrackColor = contentColorFor(bubbleColor),
//                                    thumbColor = contentColorFor(bubbleColor)
//                                ),
//                                modifier = modifier
//                                    .padding(
//                                        horizontal = 10.dp,
//                                        vertical = if (sandwichPosition == SandwichPosition.SOLO) 10.dp else 1.dp
//                                    )
//                                    .widthIn(max = configuration.screenWidthDp.dp * 0.8f)
//                                    .clip(RoundedCornerShape(24.dp))
//                                    .background(bubbleColor)
//                                    .align(alignment)
//                            )
//                        }
//                        details.uri.isVideo(context) -> {
//                            VideoAttachment(
//                                video = details.uri,
//                                modifier = Modifier
//                                    .padding(
//                                        vertical = 2.dp,
//                                        horizontal = 10.dp,
//                                    )
//                                    .widthIn(max = configuration.screenWidthDp.dp * 0.7f)
//                                    .align(alignment)
//                            )
//                        }
//                        else -> {
//                            val fileSize = remember { context.getMMSSize(cuteMessage.attachment.attachmentDetails[index].uri) }
//
//
//                            Box(
//                                modifier = Modifier
//                                    .padding(
//                                        horizontal = 10.dp,
//                                        vertical = if (sandwichPosition == SandwichPosition.SOLO) 10.dp else 1.dp
//                                    )
//                                    .widthIn(max = configuration.screenWidthDp.dp * 0.8f)
//                                    .clip(RoundedCornerShape(24.dp))
//                                    .background(bubbleColor)
//                                    .align(alignment)
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(10.dp),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
//                                ) {
//                                    Icon(
//                                        painter = painterResource(R.drawable.file),
//                                        contentDescription = null
//                                    )
//                                    Column {
//                                        Text(
//                                            text = cuteMessage.attachment.attachmentDetails[index].filename,
//                                            maxLines = 1,
//                                            modifier = Modifier.basicMarquee()
//                                        )
//                                        Text(
//                                            text = Formatter.formatFileSize(
//                                                context,
//                                                fileSize
//                                            )
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//
//        }
//        if (if (!cuteMessage.isMms) cuteMessage.body.isNotEmpty() else cuteMessage.attachment?.body?.isNotEmpty() == true) {
//            Box(
//                modifier = Modifier
//                    .padding(
//                        horizontal = 10.dp,
//                        vertical = if (sandwichPosition == SandwichPosition.SOLO) 10.dp else 1.dp
//                    )
//                    .widthIn(max = configuration.screenWidthDp.dp * 0.8f)
//                    .background(
//                        color = bubbleColor,
//                        shape = BubbleShape(
//                            sandwichPosition = sandwichPosition,
//                            messageType = cuteMessage.type
//                        )
//                    )
//                    .align(alignment)
//                    .thenIf(cuteMessage.body.isLink()) {
//                        Modifier.clickable {
//                            uriHandler.openUri(cuteMessage.body)
//                        }
//                    }
//
//            ) {
//                Text(
//                    text = if (!cuteMessage.isMms) cuteMessage.body else cuteMessage.attachment?.body ?: "",
//                    modifier = Modifier
//                        .padding(10.dp),
//                    style = MaterialTheme.typography.bodyLargeEmphasized.copy(
//                        fontSize = if (cuteMessage.body.isEmoji()) 35.sp else TextUnit.Unspecified,
//                        color = contentColorFor(bubbleColor),
//                        textDecoration = if (cuteMessage.body.isLink()) TextDecoration.Underline else null,
//                    )
//                )
//            }
//        }
//
//        if (cuteMessage.type == Telephony.Sms.MESSAGE_TYPE_FAILED) {
//            Text("Not sent.", color = MaterialTheme.colorScheme.error)
//        }
//
//
//        AnimatedVisibility(
//            visible = isTimestampVisible && !isSelected,
//            modifier = Modifier
//                .align(alignment)
//                .padding(horizontal = 10.dp)
//        ) {
//            Text(
//                text = cuteMessage.date.toTime(),
//                style = MaterialTheme.typography.bodyMediumEmphasized.copy(
//                    MaterialTheme.colorScheme.onSurfaceVariant
//                ),
//                modifier = Modifier
//                    .background(
//                        color = MaterialTheme.colorScheme.background,
//                        shape = RoundedCornerShape(50)
//                    )
//                    .padding(5.dp)
//            )
//        }
//    }
}

@Composable
private fun TextBubbleContent(
    text: String,
    type: Int,
    isScheduled: Boolean,
    sandwichPosition: SandwichPosition,
    bubbleColor: Color
) {

    val uriHandler = LocalUriHandler.current
    val shape = BubbleShape(
        sandwichPosition = sandwichPosition,
        messageType = type
    )

    Box(
        modifier = Modifier
            .background(
                color = bubbleColor,
                shape = shape
            )
            .thenIf(text.isLink()) {
                Modifier.clickable {
                    uriHandler.openUri(text)
                }
            }
            .then(
                if (isScheduled) {
                    Modifier.dashedBorder(shape)
                } else Modifier
            )

    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(10.dp),
            style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                fontSize = if (text.isEmoji()) 35.sp else TextUnit.Unspecified,
                color = contentColorFor(bubbleColor),
                textDecoration = if (text.isLink()) TextDecoration.Underline else null,
            )
        )
    }
}

enum class SandwichPosition {
    SOLO, TOP, MIDDLE, BOTTOM
}

@Composable
fun BubbleShape(
    sandwichPosition: SandwichPosition,
    messageType: Int
): Shape {

    return if (messageType == Telephony.Sms.MESSAGE_TYPE_SENT) {

        val topEnd by animateDpAsState(
            when (sandwichPosition) {
                SandwichPosition.SOLO, SandwichPosition.TOP -> 24.dp
                else -> 4.dp
            }
        )

        val bottomEnd by animateDpAsState(
            when (sandwichPosition) {
                SandwichPosition.SOLO, SandwichPosition.BOTTOM -> 24.dp
                else -> 4.dp
            }
        )

        RoundedCornerShape(
            topStart = 24.dp,
            bottomStart = 24.dp,
            topEnd = topEnd,
            bottomEnd = bottomEnd
        )
    } else {

        val topStart by animateDpAsState(
            when (sandwichPosition) {
                SandwichPosition.SOLO, SandwichPosition.TOP -> 24.dp
                else -> 4.dp
            }
        )

        val bottomStart by animateDpAsState(
            when (sandwichPosition) {
                SandwichPosition.SOLO, SandwichPosition.BOTTOM -> 24.dp
                else -> 4.dp
            }
        )

        RoundedCornerShape(
            topStart = topStart,
            bottomStart = bottomStart,
            topEnd = 24.dp,
            bottomEnd = 24.dp
        )
    }
}

@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    isPlaying: Boolean,
    onClick: () -> Unit
) {

    val interactionSource = rememberInteractionSource()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f
    )



    IconButton(
        onClick = onClick,
        modifier = buttonModifier,
        interactionSource = interactionSource
    ) {
        Icon(
            painter = if (isPlaying) painterResource(R.drawable.pause_filled) else painterResource(R.drawable.play_filled),
            contentDescription = null,
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

