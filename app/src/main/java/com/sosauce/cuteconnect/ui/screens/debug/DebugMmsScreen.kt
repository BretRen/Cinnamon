package com.sosauce.cuteconnect.ui.screens.debug

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.domain.model.CuteMms
import androidx.compose.material3.Text
@Composable
fun DebugMmsScreen(
    cuteMms: List<CuteMms>
) {


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = cuteMms,
            key = { it.id }
        ) {

            Log.d("mmsTest", "text: ${it.text}")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                AsyncImage(
                    model = Uri.parse(it.imagePath),
                    contentDescription = null
                )

                if (it.text != null) { Text(it.text) }
            }
        }
    }

}