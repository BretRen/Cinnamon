@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.phone.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sosauce.cuteconnect.domain.model.AudioRoute
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.utils.getItemShape

@Composable
fun AudioSwitcher(
    onCallAction: (CallAction) -> Unit,
    routes: List<AudioRoute>
) {
    LazyColumn {
        itemsIndexed(
            items = routes
        ) { index, route ->
            DropdownMenuItem(
                onClick = { onCallAction(CallAction.SwitchAudioTarget(route)) },
                shape = MenuDefaults.getItemShape(index, routes.lastIndex),
                text = { Text(route.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(route.type.routeToIcon()),
                        contentDescription = null
                    )
                }
            )
        }
    }
}