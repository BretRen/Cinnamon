package com.sosauce.cuteconnect.ui.screens.contacts.components

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.shared_components.CuteDropdownMenuItem
import androidx.compose.material3.Text
// TODO: Cleanup logic, we want a contacts detail class and just pass this here
@Composable
fun ContactTextField(
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    isEditMode: Boolean
) {

    val textFieldState = rememberTextFieldState()

    AnimatedContent(
        targetState = isEditMode
    ) { editMode ->
        if (!editMode) {
            CuteDropdownMenuItem(
                onClick = onClick,
                leadingIcon = leadingIcon,
                text = text
            )
        } else {
            OutlinedTextField(
                state = textFieldState,
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircle,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    }
}