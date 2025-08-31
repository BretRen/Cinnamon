@file:OptIn(ExperimentalUuidApi::class)

package com.sosauce.cuteconnect.ui.screens.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.components.AboutMeCard
import com.sosauce.cuteconnect.ui.shared_components.CuteSearchbar
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ContactsScreen(
    contacts: List<CuteContact>,
    onNavigate: (Screen) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold { paddingValues ->
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
                    items = contacts,
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

        CuteSearchbar(
            modifier = Modifier.align(rememberSearchbarAlignment()),
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowOutward,
                            contentDescription = "sort"
                        )
                    }
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "settings"
                        )
                    }
                }
            },
            fab = {
                SmallFloatingActionButton(
                    onClick = {}
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
}