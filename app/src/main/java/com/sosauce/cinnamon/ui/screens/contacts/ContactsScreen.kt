@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.cinnamon.utils.CuteRoundedCornerShape
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
                                painter = painterResource(R.drawable.add),
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
                groupedContactsList(
                    contacts = state.contacts,
                    onContactClicked = { onNavigate(Screen.ContactDetails(it.id)) }
                )
            }
        }
    }
}

fun LazyListScope.groupedContactsList(
    contacts: List<CuteContact>,
    onContactClicked: (CuteContact) -> Unit
) {
    contacts.groupBy {
        if (it.isFavorite) '*' else (it.displayName.firstOrNull()?.uppercaseChar() ?: '#')
    }.toSortedMap().forEach { (letter, contacts) ->
        item {
            if (letter == '*') {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.favorite_filled),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = pluralStringResource(R.plurals.favorites, contacts.size),
                        style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            } else {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
        itemsIndexed(
            items = contacts,
            key = { _, contact -> contact.id }
        ) { index, contact ->
            ContactListItem(
                contact = contact,
                modifier = Modifier.animateItem(),
                onContactClick = { onContactClicked(contact) },
                shape = CuteRoundedCornerShape(
                    top = if (index == 0) 24.dp else 4.dp,
                    bottom = if (index == contacts.lastIndex) 24.dp else 0.dp
                )
            )
        }
    }
}