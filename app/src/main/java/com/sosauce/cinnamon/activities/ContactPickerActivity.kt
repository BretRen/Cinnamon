@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.activities

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.presentation.screens.contacts.ContactsViewModel
import com.sosauce.cinnamon.presentation.screens.contacts.groupedContactsList
import com.sosauce.cinnamon.presentation.shared_components.NoXFound
import org.koin.androidx.compose.koinViewModel

class ContactPickerActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            val contactsViewModel = koinViewModel<ContactsViewModel>()
            val state by contactsViewModel.state.collectAsStateWithLifecycle()


            SharedTransitionLayout {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { ContainedLoadingIndicator() }
                } else {
                    Scaffold { paddingValues ->
                        LazyColumn(
                            contentPadding = paddingValues
                        ) {
                            groupedContactsList(
                                contacts = state.contacts,
                                onContactClicked = {
                                    val resultIntent = Intent().apply {
                                        putExtra("contact_id", it.id)
                                    }
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                emptyState = {
                                    NoXFound(
                                        headlineText = R.string.no_contacts_found,
                                        bodyText = R.string.no_widget_for_u,
                                        icon = R.drawable.contacts
                                    )
                                }
                            )
                        }
                    }

                }
            }

        }


    }



}