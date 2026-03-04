package com.sosauce.cuteconnect.ui.screens.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.buildSpannedString
import com.sosauce.cuteconnect.domain.states.CallState
import com.sosauce.cuteconnect.ui.screens.phone.components.CallBottomBar
import com.sosauce.cuteconnect.ui.screens.phone.components.IncomingBottomBar
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.utils.beautifyNumber
import com.sosauce.cuteconnect.utils.getContactId
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.getContactPfpUriFromId
import com.sosauce.cuteconnect.utils.toStopwatch
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CallScreen(
    onCallAction: (CallAction) -> Unit,
    callUiState: CallingState
) {

    val context = LocalContext.current
    val displayName = remember(callUiState.number) {
        callUiState.number.getContactNameOrNothing(context).beautifyNumber()
    }

    Scaffold(
        bottomBar = {
            if (callUiState.callState == CallState.RINGING) {
                IncomingBottomBar(
                    onCallAction = onCallAction
                )
            } else {
                CallBottomBar(
                    onCallAction = onCallAction,
                    callUiState = callUiState
                )
            }
        }
    ) { paddingValues ->
//        AsyncImage(
//            model = ImageUtils.imageRequester(callUiState.poster.toUri(), context),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.fillMaxSize()
//        )
//        Image(
//            painter = painterResource(R.drawable.wallpaper_test),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.fillMaxSize()
//        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DefaultContactIcon(
                firstLetter = displayName.firstOrNull(),
                size = 150.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contactPfp = callUiState.number.getContactId(context).getContactPfpUriFromId()
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = displayName,
                maxLines = 1,
                style = MaterialTheme.typography.displaySmallEmphasized.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.basicMarquee()
            )
            Spacer(Modifier.height(10.dp))

            val secondaryText = when(callUiState.callState) {
                CallState.RINGING -> buildAnnotatedString {
                    append("via ")
                    withStyle(SpanStyle(color = Color(callUiState.activeSim.color))) {
                        append(callUiState.activeSim.name)
                    }
                }
                CallState.DIALING -> AnnotatedString("Ringing...")
                CallState.ENDED -> AnnotatedString("Call ended")
                CallState.ONGOING -> AnnotatedString(callUiState.timeSpentInCall.toStopwatch(DurationUnit.SECONDS))
            }
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            AnimatedVisibility(
                visible = callUiState.isHolding,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Text(
                    text = "On hold",
                    style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                        MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}