@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.wallpaper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.core.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import com.sosauce.cuteconnect.ui.shared_components.AnimatedSlider
import com.sosauce.cuteconnect.ui.shared_components.CategoryCard
import com.sosauce.cuteconnect.ui.shared_components.CuteNavigationButton
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.ui.shared_components.text.HeaderText
import com.sosauce.cuteconnect.utils.ImageUtils
import com.sosauce.cuteconnect.utils.addOrNot
import com.sosauce.cuteconnect.utils.copyMutate
import com.sosauce.cuteconnect.viewModels.ConversationViewModel
import com.squareup.okhttp.internal.framed.Header
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationTheming(
    threadId: Long,
    onPopBackStack: () -> Unit
) {

    val context = LocalContext.current
    val convoViewModel = koinViewModel<ConversationViewModel>()
    val convoSettings by convoViewModel.getConversationSettings(threadId).collectAsStateWithLifecycle(ConversationSettings())
    var blurIntensityValue by remember(convoSettings.wallpaperBlurIntensity) { mutableStateOf(convoSettings.wallpaperBlurIntensity) }
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            onDismissRequest = { showColorPicker = false },
            onAddNewColor = { color ->
                convoViewModel.handleConversationSettingsActions(
                    ConversationSettingActions.UpsertConversationSettings(
                        convoSettings.copy(
                            allColors = convoSettings.allColors.copyMutate { addOrNot(color) }
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

                    AddWallpaperItem(
                        modifier = Modifier.padding(4.dp),
                        onAddNewWallpaper = { wallpaperUri ->
                            convoViewModel.handleConversationSettingsActions(
                                ConversationSettingActions.UpsertConversationSettings(
                                    convoSettings.copy(
                                        allWallpapers = convoSettings.allWallpapers.copyMutate { addOrNot(wallpaperUri.toString()) },
                                    )
                                )
                            )
                        }
                    )
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { convoSettings.allWallpapers.count() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 16.dp, bottom = 16.dp),
                        preferredItemWidth = 186.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { index ->
                        val wallpaper = convoSettings.allWallpapers[index]
                        WallpaperItem(
                            wallpaper = wallpaper,
                            isCurrentWallpaper = wallpaper == convoSettings.wallpaper,
                            onSetAsWallpaper = {
                                convoViewModel.handleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        convoSettings.copy(
                                            wallpaper = wallpaper
                                        )
                                    )
                                )
                            },
                            blurIntensity = convoSettings.wallpaperBlurIntensity
                        )
                    }

//                    LazyRow {
//                        item {
//                            AddWallpaperItem(
//                                modifier = Modifier.padding(4.dp),
//                                onAddNewWallpaper = { wallpaperUri ->
//                                    convoViewModel.handleConversationSettingsActions(
//                                        ConversationSettingActions.UpsertConversationSettings(
//                                            convoSettings.copy(
//                                                allWallpapers = convoSettings.allWallpapers.copyMutate { addOrNot(wallpaperUri.toString()) },
//                                            )
//                                        )
//                                    )
//                                }
//                            )
//                        }
//                        items(
//                            items = convoSettings.allWallpapers,
//                            key = { it }
//                        ) { wallpaper ->
//                            WallpaperItem(
//                                modifier = Modifier
//                                    .padding(5.dp)
//                                    .animateItem(),
//                                wallpaper = wallpaper,
//                                isCurrentWallpaper = wallpaper == convoSettings.wallpaper,
//                                onSetAsWallpaper = {
//                                    convoViewModel.handleConversationSettingsActions(
//                                        ConversationSettingActions.UpsertConversationSettings(
//                                            convoSettings.copy(
//                                                wallpaper = wallpaper
//                                            )
//                                        )
//                                    )
//                                },
//                                blurIntensity = convoSettings.wallpaperBlurIntensity
//                            )
//                        }
//                    }
                }
                HeaderText("Blur intensity (${blurIntensityValue.value.fastRoundToInt()}%)")
                CategoryCard(
                    topDp = 24.dp,
                    bottomDp = 24.dp
                ) {
                    AnimatedSlider(
                        value = blurIntensityValue.value,
                        onValueChanged = { blurIntensityValue = it.dp },
                        onValueChangeFinished = {
                            convoViewModel.handleConversationSettingsActions(
                                ConversationSettingActions.UpsertConversationSettings(
                                    convoSettings.copy(
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
                            items = convoSettings.allColors.reversed(),
                            key = { it }
                        ) { color ->
                            IconButton(
                                onClick = {
                                    convoViewModel.handleConversationSettingsActions(
                                        ConversationSettingActions.UpsertConversationSettings(
                                            convoSettings.copy(
                                                convoId = if (convoSettings.convoId == 0L) threadId else convoSettings.convoId,
                                                color = color
                                            )
                                        )
                                    )
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(color).copy(0.85f)
                                ),
                                modifier = Modifier.padding(5.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = color == convoSettings.color,
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (convoSettings.color.luminance > 0.5f) Color.Black else Color.White
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
                onNavigateUp = onPopBackStack
            )
        }
    }

}

