package com.sosauce.cuteconnect.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.data.contact_settings.ContactSettings
import com.sosauce.cuteconnect.data.contact_settings.ContactSettingsActions
import com.sosauce.cuteconnect.data.contact_settings.ContactSettingsDao
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.domain.repository.ContactDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ContactDetailsViewModel(
    private val contactId: Long,
    private val contactDetailsRepository: ContactDetailsRepository,
    private val contactSettingsDao: ContactSettingsDao
): ViewModel() {

    private val _state = MutableStateFlow(ContactDetailsState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                launch {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            contact = contactDetailsRepository.fetchContactDetails(contactId)
                        )
                    }
                }
                launch {
                    contactSettingsDao.getConversationSettings(contactId).collectLatest { settings ->
                        _state.update {
                            it.copy(
                                settings = settings ?: ContactSettings(contactId.toInt())
                            )
                        }
                    }
                }
            }
        }
    }

    fun handleContactSettingsAction(action: ContactSettingsActions) {
        when(action) {
            is ContactSettingsActions.UpsertContactSettings -> {
                viewModelScope.launch(Dispatchers.IO) {
                    contactSettingsDao.upsertContact(action.contactSettings)
                }
            }
        }
    }

}

data class ContactDetailsState(
    val isLoading: Boolean = false,
    val contact: CuteContact = CuteContact(),
    val settings: ContactSettings = ContactSettings()
)