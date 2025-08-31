package com.sosauce.cuteconnect.ui.screens.phone

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sosauce.cuteconnect.domain.states.CallUiState
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.viewModels.CallViewModel

@Composable
fun IncomingScreen(
    callViewModel: CallViewModel,
    callUiState: CallUiState
) {
    val context = LocalContext.current
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
                .fillMaxSize()
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
            CuteText(
                text = displayName,
                fontSize = 30.sp,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = callViewModel::declineCall,
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(0.85f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        modifier = Modifier.rotate(135f)
                    )
                }
                Button(
                    onClick = callViewModel::answerCall,
                    modifier = Modifier.size(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green.copy(0.85f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null
                    )
                }
            }
        }
    }
}