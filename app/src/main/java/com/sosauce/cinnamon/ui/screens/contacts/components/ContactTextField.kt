@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.contacts.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sosauce.cinnamon.R

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
            DropdownMenuItem(
                onClick = onClick,
                shape = MenuDefaults.standaloneItemShape,
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
                            painter = painterResource(R.drawable.close),
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    }
}