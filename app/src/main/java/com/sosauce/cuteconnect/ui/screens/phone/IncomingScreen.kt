@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.phone

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.utils.getContactNameOrNothing

@Composable
fun IncomingScreen(
    onCallActions: (CallAction) -> Unit,
    callingState: CallingState
) {
    val context = LocalContext.current
    val interactionSources = List(2) { remember { MutableInteractionSource() } }
    val displayName = remember(callingState.number) {
        callingState.number.getContactNameOrNothing(context)
    }

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DefaultContactIcon(
                firstLetter = displayName.firstOrNull(),
                size = 200.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialShapes.Cookie12Sided.toShape()
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineLargeEmphasized.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Spacer(Modifier.weight(1f))
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.padding(bottom = 50.dp)
            ) {
                customItem(
                    {
                        IconButton(
                            onClick = { onCallActions(CallAction.DeclineCall) },
                            interactionSource = interactionSources[0],
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.error)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSources[0])
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.call),
                                contentDescription = null,
                                modifier = Modifier.rotate(135f)
                            )
                        }
                    },
                    {}
                )
                customItem(
                    {
                        IconButton(
                            onClick = { onCallActions(CallAction.AnswerCall) },
                            interactionSource = interactionSources[1],
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryFixed,
                                contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.tertiaryFixed)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSources[1])
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.phone_filled),
                                contentDescription = null
                            )
                        }
                    },
                    {}
                )
            }
        }
    }
}

