@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.contact_settings.ContactSettingsActions
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.screens.contacts.components.ContactActionsRow
import com.sosauce.cinnamon.ui.screens.contacts.components.ContactInfos
import com.sosauce.cinnamon.ui.screens.phone.CallAction
import com.sosauce.cinnamon.ui.shared_components.BottomActionButtons
import com.sosauce.cinnamon.ui.shared_components.DefaultContactIcon
import com.sosauce.cinnamon.ui.shared_components.ImagePickerCard
import com.sosauce.cinnamon.ui.shared_components.buttons.CuteNavigationButtonSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ContactDetails(
    state: ContactDetailsState,
    onNavigateBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onHandleContactSettingsAction: (ContactSettingsActions) -> Unit,
    onHandleContactDetailsAction: (ContactDetailsAction) -> Unit
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
        val scope = rememberCoroutineScope()
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

            if (uri == null) return@rememberLauncherForActivityResult

            scope.launch(Dispatchers.IO) {

                File(state.settings.poster).delete()

                val file = File(context.filesDir, "poster_${state.contact.id}_${System.currentTimeMillis()}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }

                onHandleContactSettingsAction(
                    ContactSettingsActions.UpsertContactSettings(
                        state.settings.copy(
                            poster = file.path
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
                        .navigationBarsPadding()
                        .padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CuteNavigationButtonSurface(onNavigateUp = onNavigateBack)
                    BottomActionButtons {
                        Row {
                            IconButton(
                                onClick = { isEditMode = !isEditMode }
                            ) {
                                AnimatedContent(
                                    targetState = isEditMode
                                ) { editMode ->
                                    if (editMode) {
                                        Icon(
                                            painter = painterResource(R.drawable.close),
                                            contentDescription = null
                                        )
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
                SharedTransitionLayout {
                    AnimatedContent(isEditMode) { isEdit ->
                        if (!isEdit) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = state.settings.poster,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(200.dp)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState("poster"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .cloudy(30),
                                    contentScale = ContentScale.FillWidth
                                )
                                DefaultContactIcon(
                                    firstLetter = state.contact.firstName.firstOrNull(),
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState("pfp"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ),
                                    size = 170.dp,
                                    contactPfp = state.contact.photo,
                                    shape = MaterialShapes.Cookie9Sided.toShape()
                                )

                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DefaultContactIcon(
                                    firstLetter = state.contact.firstName.firstOrNull(),
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState("pfp"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ),
                                    size = 170.dp,
                                    contactPfp = state.contact.photo,
                                    shape = MaterialShapes.Cookie9Sided.toShape()
                                )
                                ImagePickerCard(
                                    onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    onRemoveImage = {
                                        scope.launch(Dispatchers.IO) {
                                            File(context.filesDir, state.settings.poster).delete()
                                            onHandleContactSettingsAction(
                                                ContactSettingsActions.UpsertContactSettings(
                                                    state.settings.copy(
                                                        poster = ""
                                                    )
                                                )
                                            )
                                        }
                                    },
                                    imagePath = state.settings.poster,
                                    modifier = Modifier
                                        .height(250.dp)
                                        .width(150.dp)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState("poster"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        )
                                )

                            }
                        }
                    }
                }
                Spacer(Modifier.height(15.dp))
                Text(
                    text = state.contact.firstName,
                    modifier = Modifier.basicMarquee(),
                    style = MaterialTheme.typography.headlineLargeEmphasized
                )
                Spacer(Modifier.height(15.dp))
                ContactActionsRow(
                    contact = state.contact,
                    onNavigate = onNavigate,
                    onHandleCallAction = onHandleCallAction,
                    onHandleContactDetailsAction = onHandleContactDetailsAction
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