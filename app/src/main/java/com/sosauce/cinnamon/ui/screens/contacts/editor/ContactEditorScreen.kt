@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.contacts.editor

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.ui.shared_components.buttons.CuteNavigationButton

@Composable
fun ContactEditorScreen(
    contact: CuteContact,
    onSave: (CuteContact) -> Unit,
    onNavigateUp: () -> Unit
) {
    var newContact by remember { mutableStateOf(contact) }
    val scrollState = rememberScrollState()
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = contact != newContact) {
        showUnsavedChangesDialog = true
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            confirmButton = {},
            text = { Text("Unsaved changes detected") }
        )
    }


    Scaffold(
        bottomBar = {
            CuteNavigationButton(
                onNavigateUp = {
                    if (contact != newContact) {
                        showUnsavedChangesDialog = true
                    } else onNavigateUp()
                }
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(pv)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // --- Photo ---
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(42.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (newContact.photo != Uri.EMPTY) {
                    AsyncImage(
                        model = newContact.photo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Name ---
            OutlinedTextField(
                value = newContact.firstName,
                onValueChange = { newContact = newContact.copy(firstName = it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.person_filled), contentDescription = null)
                }
            )

            Spacer(Modifier.height(8.dp))

            // --- Phone Numbers ---
            SectionHeader(title = "Phone")
            newContact.phoneNumbers.forEachIndexed { index, phone ->
                FieldRow(
                    value = phone.number,
                    label = "Phone",
                    leadingIcon = R.drawable.phone,
                    onValueChange = {
                        val updated = newContact.phoneNumbers.toMutableList()
                        updated[index] = phone.copy(number = it)
                        newContact = newContact.copy(phoneNumbers = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            phoneNumbers = newContact.phoneNumbers.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add phone") {
                newContact = newContact.copy(
                    phoneNumbers = newContact.phoneNumbers + CuteContact.Phone("", 1, false)
                )
            }

            // --- Emails ---
            SectionHeader(title = "Email")
            newContact.emails.forEachIndexed { index, email ->
                FieldRow(
                    value = email.email,
                    label = "Email",
                    leadingIcon = R.drawable.email,
                    onValueChange = {
                        val updated = newContact.emails.toMutableList()
                        updated[index] = email.copy(email = it)
                        newContact = newContact.copy(emails = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            emails = newContact.emails.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add email") {
                newContact = newContact.copy(
                    emails = newContact.emails + CuteContact.Email("", 1, false)
                )
            }

            // --- Addresses ---
            SectionHeader(title = "Address")
            newContact.addresses.forEachIndexed { index, address ->
                FieldRow(
                    value = address.address,
                    label = "Address",
                    leadingIcon = R.drawable.address,
                    onValueChange = {
                        val updated = newContact.addresses.toMutableList()
                        updated[index] = address.copy(address = it)
                        newContact = newContact.copy(addresses = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            addresses = newContact.addresses.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add address") {
                newContact = newContact.copy(
                    addresses = newContact.addresses + CuteContact.Address("", 1, false)
                )
            }

            // --- Websites ---
            SectionHeader(title = "Website")
            newContact.websites.forEachIndexed { index, website ->
                FieldRow(
                    value = website.website,
                    label = "Website",
                    leadingIcon = R.drawable.website,
                    onValueChange = {
                        val updated = newContact.websites.toMutableList()
                        updated[index] = website.copy(website = it)
                        newContact = newContact.copy(websites = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            websites = newContact.websites.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add website") {
                newContact = newContact.copy(
                    websites = newContact.websites + CuteContact.Website("")
                )
            }

            // --- Notes ---
            SectionHeader(title = "Notes")
            newContact.notes.forEachIndexed { index, note ->
                FieldRow(
                    value = note.note,
                    label = "Note",
                    leadingIcon = R.drawable.note,
                    singleLine = false,
                    onValueChange = {
                        val updated = newContact.notes.toMutableList()
                        updated[index] = note.copy(note = it)
                        newContact = newContact.copy(notes = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            notes = newContact.notes.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add note") {
                newContact = newContact.copy(
                    notes = newContact.notes + CuteContact.Note("")
                )
            }

            // --- Events ---
            SectionHeader(title = "Events")
            newContact.events.forEachIndexed { index, event ->
                FieldRow(
                    value = event.date,
                    label = "Date (e.g. 1990-01-01)",
                    leadingIcon = R.drawable.event,
                    onValueChange = {
                        val updated = newContact.events.toMutableList()
                        updated[index] = event.copy(date = it)
                        newContact = newContact.copy(events = updated)
                    },
                    onRemove = {
                        newContact = newContact.copy(
                            events = newContact.events.toMutableList().also { it.removeAt(index) }
                        )
                    }
                )
            }
            AddFieldButton(label = "Add event") {
                newContact = newContact.copy(
                    events = newContact.events + CuteContact.Event("", 1)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun FieldRow(
    value: String,
    label: String,
    leadingIcon: Int,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    singleLine: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = singleLine,
            leadingIcon = {
                Icon(painter = painterResource(leadingIcon), contentDescription = null)
            }
        )
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddFieldButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.add),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}