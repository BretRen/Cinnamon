@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.wallpaper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.luminance
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.ui.shared_components.AnimatedSlider
import com.sosauce.cuteconnect.ui.shared_components.CategoryCard
import com.sosauce.cuteconnect.ui.shared_components.buttons.CuteNavigationButton
import com.sosauce.cuteconnect.ui.shared_components.text.HeaderText
import com.sosauce.cuteconnect.utils.addOrNot
import com.sosauce.cuteconnect.utils.copyMutate

@Composable
fun ConversationTheming(
    state: ThemingState,
    onHandleConversationSettingsActions: (ConversationSettingActions) -> Unit,
    onNavigateBack: () -> Unit
) {

    val context = LocalContext.current
    var blurIntensityValue by remember(state.settings.wallpaperBlurIntensity) { mutableIntStateOf(state.settings.wallpaperBlurIntensity) }
    var showColorPicker by remember { mutableStateOf(false) }

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

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                HeaderText("Wallpaper")
                CategoryCard(
                    topDp = 24.dp,
                    bottomDp = 24.dp
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {

                        AddWallpaperItem(
                            onAddNewWallpaper = { wallpaperUri ->
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(
                                            allWallpapers = state.settings.allWallpapers.copyMutate { addOrNot(wallpaperUri.toString()) },
                                        )
                                    )
                                )
                            }
                        )

                        FilledIconButton(
                            onClick = {
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(
                                            wallpaper = ""
                                        )
                                    )
                                )
                            },
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = contentColorFor(MaterialTheme.colorScheme.errorContainer)
                            ),
                            modifier = Modifier.size(IconButtonDefaults.smallContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Wide))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete_filled),
                                contentDescription = null
                            )
                        }
                    }

                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { state.settings.allWallpapers.count() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 16.dp, bottom = 16.dp),
                        preferredItemWidth = 186.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { index ->
                        val wallpaper = state.settings.allWallpapers[index]
                        WallpaperItem(
                            wallpaper = wallpaper,
                            isCurrentWallpaper = wallpaper == state.settings.wallpaper,
                            onSetAsWallpaper = {
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(
                                            wallpaper = wallpaper
                                        )
                                    )
                                )
                            },
                            onDeleteWallpaper = {
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(
                                            allWallpapers = state.settings.allWallpapers.copyMutate { remove(wallpaper) }
                                        )
                                    )
                                )
                            },
                            blurIntensity = state.settings.wallpaperBlurIntensity
                        )
                    }

                }
                HeaderText("Blur intensity (${blurIntensityValue}%)")
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
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (state.settings.color.luminance > 0.5f) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            CuteNavigationButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding(),
                onNavigateUp = onNavigateBack
            )
        }
    }

}

