@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.components.AboutMeCard
import com.sosauce.cuteconnect.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ContactsScreen(
    state: ContactsState,
    onNavigate: (Screen) -> Unit,
) {


    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        val textFieldState = rememberTextFieldState()
        Scaffold(
            bottomBar = {
                CuteSearchbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .navigationBarsPadding(),
                    textFieldState = textFieldState,
                    sortingMenu = {},
                    fab = {
                        FloatingActionButton(
                            onClick = {},
                            shape = MaterialShapes.Cookie9Sided.toShape()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        }
                    },
                    onNavigate = onNavigate
                )
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues
            ) {
                item(
                    key = "MeCard"
                ) {
                    AboutMeCard(
                        onNavigate = onNavigate
                    )
                }
                items(
                    items = state.contacts,
                    key = { it.id }
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 4.dp),
                        onContactClick = { onNavigate(Screen.ContactDetails(contact.id)) }
                    )
                }
            }
        }
    }
}