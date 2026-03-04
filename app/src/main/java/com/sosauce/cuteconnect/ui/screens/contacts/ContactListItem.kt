@file:OptIn(ExperimentalFoundationApi::class)

package com.sosauce.cuteconnect.ui.screens.contacts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.utils.beautifyNumber

@Composable
fun ContactListItem(
    modifier: Modifier = Modifier,
    contact: CuteContact,
    onContactClick: () -> Unit,
    showNumber: Boolean = false,
    shape: Shape = RoundedCornerShape(24.dp)
) {

    Surface(
        onClick = onContactClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 1.dp,
                horizontal = 5.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultContactIcon(
                firstLetter = contact.displayName.first(),
                modifier = Modifier
                    .padding(start = 10.dp),
                contactPfp = contact.photo
            )
            Column(
                modifier = Modifier
                    .padding(start = 15.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buildString {
                        append(contact.displayName)
                        if (contact.middleName.isNotEmpty()) {
                            append(" ${contact.middleName}")
                        }
                        if (contact.lastName.isNotEmpty()) {
                            append(" ${contact.lastName}")
                        }
                    },
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                if (showNumber) {
                    Text(
                        text = contact.phoneNumbers.first().number.beautifyNumber(),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}