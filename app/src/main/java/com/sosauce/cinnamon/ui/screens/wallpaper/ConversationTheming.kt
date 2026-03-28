@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.wallpaper

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.luminance
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.conversation_settings.ConversationSettingActions
import com.sosauce.cinnamon.ui.shared_components.AnimatedSlider
import com.sosauce.cinnamon.ui.shared_components.CategoryCard
import com.sosauce.cinnamon.ui.shared_components.ImagePickerCard
import com.sosauce.cinnamon.ui.shared_components.buttons.CuteNavigationButton
import com.sosauce.cinnamon.ui.shared_components.text.HeaderText
import com.sosauce.cinnamon.utils.addOrNot
import com.sosauce.cinnamon.utils.copyMutate
import com.sosauce.cinnamon.utils.selfAlignHorizontally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ConversationTheming(
    state: ThemingState,
    threadId: Long,
    onHandleConversationSettingsActions: (ConversationSettingActions) -> Unit,
    onNavigateBack: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var blurIntensityValue by remember(state.settings.wallpaperBlurIntensity) { mutableIntStateOf(state.settings.wallpaperBlurIntensity) }
    var showColorPicker by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch(Dispatchers.IO) {

            File(state.settings.wallpaper).delete()

            val file = File(context.filesDir, "wallpaper_${threadId}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            onHandleConversationSettingsActions(
                ConversationSettingActions.UpsertConversationSettings(
                    state.settings.copy(
                        wallpaper = file.path
                    )
                )
            )
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onDismissRequest = { showColorPicker = false },
            onAddNewColor = { color ->
                onHandleConversationSettingsActions(
                    ConversationSettingActions.UpsertConversationSettings(
                        state.settings.copy(
                            allColors = state.settings.allColors.copyMutate { addOrNot(color) }
                        )
                    )
                )
            }
        )
    }

    Scaffold(
        bottomBar = {
            CuteNavigationButton(
                modifier = Modifier
                    .selfAlignHorizontally(Alignment.Start)
                    .navigationBarsPadding(),
                onNavigateUp = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            HeaderText("Wallpaper")

            ImagePickerCard(
                onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onRemoveImage = {
                    scope.launch(Dispatchers.IO) {
                        File(context.filesDir, state.settings.wallpaper).delete()
                        onHandleConversationSettingsActions(
                            ConversationSettingActions.UpsertConversationSettings(
                                state.settings.copy(
                                    wallpaper = ""
                                )
                            )
                        )
                    }
                },
                imagePath = state.settings.wallpaper,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .height(300.dp)
                    .width(150.dp)
            )
            HeaderText(stringResource(R.string.blur_intensity) + " (${blurIntensityValue}%)")
            CategoryCard(
                topDp = 24.dp,
                bottomDp = 24.dp
            ) {
                AnimatedSlider(
                    value = blurIntensityValue.toFloat(),
                    onValueChanged = { blurIntensityValue = it.toInt() },
                    onValueChangeFinished = {
                        onHandleConversationSettingsActions(
                            ConversationSettingActions.UpsertConversationSettings(
                                state.settings.copy(
                                    wallpaperBlurIntensity = blurIntensityValue,
                                )
                            )
                        )
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier
                        .padding(10.dp)
                )
            }

            HeaderText("Chat color")
            CategoryCard(
                topDp = 24.dp,
                bottomDp = 24.dp
            ) {
                LazyRow {
                    item {
                        IconButton(
                            onClick = { showColorPicker = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.colorize),
                                contentDescription = null
                            )
                        }
                    }

                    items(
                        items = state.settings.allColors.reversed(),
                        key = { it }
                    ) { color ->
                        IconButton(
                            onClick = {
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(color = color)
                                    )
                                )
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(color)
                            ),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            AnimatedVisibility(
                                visible = color == state.settings.color,
                                enter = scaleIn(),
                                exit = scaleOut()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = if (state.settings.color.luminance > 0.5f) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

