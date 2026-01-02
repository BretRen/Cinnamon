@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.contacts

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.contact_settings.ContactSettingsActions
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.components.ContactActionsRow
import com.sosauce.cuteconnect.ui.screens.contacts.components.ContactInfos
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.ui.shared_components.BottomActionButtons
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.ui.shared_components.buttons.CuteNavigationButton

@Composable
fun ContactDetails(
    state: ContactDetailsState,
    onNavigateBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onHandleContactSettingsAction: (ContactSettingsActions) -> Unit
) {

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        val context = LocalContext.current
        var isEditMode by rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                onHandleContactSettingsAction(
                    ContactSettingsActions.UpsertContactSettings(
                        state.settings.copy(
                            poster = uri.toString()
                        )
                    )
                )
            }
        }


        Scaffold(
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CuteNavigationButton(onNavigateUp = onNavigateBack)
                    BottomActionButtons {
                        Row {
                            IconButton(
                                onClick = { isEditMode = !isEditMode }
                            ) {
                                AnimatedContent(
                                    targetState = isEditMode
                                ) { editMode ->
                                    if (editMode) {
//                                        Icon(
//                                            imageVector = Icons.Rounded.Close,
//                                            contentDescription = null
//                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.edit_filled),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_filled),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        ) { pv ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(pv)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = state.settings.poster.toUri(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(200.dp)
                            .cloudy(30),
                        contentScale = ContentScale.FillWidth
                    )
                    DefaultContactIcon(
                        firstLetter = state.contact.name.firstOrNull(),
                        modifier = Modifier
                            .padding(start = 10.dp),
                        size = 170.dp,
                        contactPfp = state.contact.photo,
                        shape = MaterialShapes.Cookie9Sided.toShape()
                    )

                }
                Spacer(Modifier.height(15.dp))
                Text(
                    text = state.contact.name,
                    modifier = Modifier.basicMarquee(),
                    style = MaterialTheme.typography.headlineLargeEmphasized
                )
                Spacer(Modifier.height(15.dp))
                ContactActionsRow(
                    contact = state.contact,
                    onNavigate = onNavigate,
                    onHandleCallAction = onHandleCallAction
                )
                Spacer(Modifier.height(25.dp))
                ContactInfos(
                    contact = state.contact,
                    isEditMode = isEditMode
                )

            }
        }

    }


}