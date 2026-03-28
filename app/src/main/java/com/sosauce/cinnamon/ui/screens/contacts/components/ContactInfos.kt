@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.contacts.components

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.ui.shared_components.text.HeaderText
import com.sosauce.cinnamon.utils.formateEventDate

@Composable
fun ContactInfos(
    contact: CuteContact,
    isEditMode: Boolean
) {

    val resources = LocalResources.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column {
        HeaderText("Contact info")
        Card(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                contact.phoneNumbers.forEachIndexed { index, number ->

                    ContactTextField(
                        onClick = {},
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.phone),
                                    contentDescription = null
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(number.number)
                                Text(
                                    text = buildString {
                                        append(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, number.type, "Custom"))
                                        if (number.isDefault) {
                                            append(" · Default")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        },
                        isEditMode = isEditMode
                    )

//                    CuteDropdownMenuItem(
//                        onClick = {},
//                        leadingIcon = {
//                            Icon(
//                                painter = painterResource(R.drawable.phone),
//                                contentDescription = null,
//                                tint = if (index == 0) LocalContentColor.current else Color.Transparent
//                            )
//                        },
//                        text = {
//                            Column {
//                                Text(number.number)
//                                Text(
//                                    text = buildString {
//                                        append(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, number.type, "Custom"))
//                                        if (number.isDefault) {
//                                            append(" · Default")
//                                        }
//                                    },
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    fontSize = 13.sp
//                                )
//                            }
//                        }
//                    )
                }


                contact.emails.forEachIndexed { index, email ->
                    DropdownMenuItem(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
                                .apply {
                                    putExtra(Intent.EXTRA_EMAIL, email.email)
                                }

                            context.startActivity(intent)
                        },
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.email),
                                    contentDescription = null
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(email.email)
                                Text(
                                    text = buildString {
                                        append(ContactsContract.CommonDataKinds.Email.getTypeLabel(resources, email.type, "Custom"))
                                        if (email.isDefault) {
                                            append(" · Default")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    )
                }

                contact.addresses.forEachIndexed { index, address ->
                    DropdownMenuItem(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "geo:0,0?q=${address.address}".toUri())
                                .apply {
                                    setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
                                }
                            context.startActivity(intent)
                        },
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.address),
                                    contentDescription = null
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(address.address)
                                Text(
                                    text = buildString {
                                        append(ContactsContract.CommonDataKinds.StructuredPostal.getTypeLabel(resources, address.type, "Custom"))
                                        if (address.isDefault) {
                                            append(" · Default")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    )
                }

            }
        }

        HeaderText("About ${contact.firstName}")
        Card(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                contact.websites.forEachIndexed { index, website ->
                    DropdownMenuItem(
                        onClick = { uriHandler.openUri(website.website) },
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.website),
                                    contentDescription = null
                                )
                            }
                        },
                        text = { Text(website.website) }
                    )
                }

                contact.events.forEachIndexed { index, event ->

                    DropdownMenuItem(
                        onClick = {},
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.event),
                                    contentDescription = null
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(event.date.formateEventDate())
                                Text(
                                    text = ContactsContract.CommonDataKinds.Event.getTypeLabel(resources, event.type, "Custom").toString(),
                                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    )
                }
                contact.notes.forEachIndexed { index, note ->
                    DropdownMenuItem(
                        onClick = {},
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(R.drawable.note),
                                    contentDescription = null
                                )
                            }
                        },
                        text = { Text(note.note) }
                    )
                }
            }
        }
    }
}