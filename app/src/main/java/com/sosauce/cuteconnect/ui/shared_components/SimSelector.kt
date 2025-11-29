@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.shared_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.datastore.rememberDefaultSimCard
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.ui.navigation.LocalHazeState
import androidx.compose.material3.Text
import androidx.compose.ui.util.fastForEach
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun SimSelector(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    cuteSimCards: List<CuteSimCard>
) {
    var defaultSimCard by rememberDefaultSimCard()

    if (visible) {
        AlertDialog(
            modifier = Modifier
                .clip(AlertDialogDefaults.shape),
//                .hazeEffect(
//                    state = LocalHazeState.current,
//                    style = HazeMaterials.thick(
//                        containerColor = MaterialTheme.colorScheme.surfaceContainer
//                    )
//                ),
            //containerColor = Color.Transparent,
            onDismissRequest = onDismissRequest,
            title = { Text("Select a default SIM") },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.SimCard,
                    contentDescription = null
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDismissRequest,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.okay))
                }
            },
            text = {
                cuteSimCards.fastForEach { card ->
                    CuteDropdownMenuItem(
                        leadingIcon = {
                            RadioButton(
                                selected = card.subId == defaultSimCard,
                                onClick = null
                            )
                        },
                        text = {
                            Column {
                                Text(card.name)
                                Text(
                                    text = card.carrierName,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.SimCard,
                                contentDescription = null,
                                tint = Color(card.color)
                            )
                        }
                    )
                }
            }
        )
    }
}