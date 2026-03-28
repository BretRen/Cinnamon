package com.sosauce.cinnamon.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cinnamon.data.contact_settings.ContactSettings
import com.sosauce.cinnamon.data.contact_settings.ContactSettingsActions
import com.sosauce.cinnamon.data.contact_settings.ContactSettingsDao
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.domain.repository.ContactDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactDetailsViewModel(
    private val contactId: Long,
    private val contactDetailsRepository: ContactDetailsRepository,
    private val contactSettingsDao: ContactSettingsDao
): ViewModel() {


    val state = combine(
        contactDetailsRepository.fetchLatestContactDetails(contactId),
        contactSettingsDao.getConversationSettings(contactId)
    ) { details, settings ->
        ContactDetailsState(
            isLoading = false,
            contact = details,
            settings = settings ?: ContactSettings(id = contactId.toInt())
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ContactDetailsState(isLoading = true)
    )

    fun handleContactSettingsAction(action: ContactSettingsActions) {
        when(action) {
            is ContactSettingsActions.UpsertContactSettings -> {
                viewModelScope.launch(Dispatchers.IO) {
                    contactSettingsDao.upsertContact(action.contactSettings)
                }
            }
        }
    }

    fun handleContactDetailsAction(action: ContactDetailsAction) {
        when(action) {
            is ContactDetailsAction.ToggleFavorite -> {
                viewModelScope.launch {
                    contactDetailsRepository.toggleFavorite(contactId, action.isFavorite)
                }
            }
            is ContactDetailsAction.ShareContact -> {}
        }
    }

}

data class ContactDetailsState(
    val isLoading: Boolean = false,
    val contact: CuteContact = CuteContact(),
    val settings: ContactSettings = ContactSettings()
)

sealed interface ContactDetailsAction {
    data class ToggleFavorite(val isFavorite: Boolean) : ContactDetailsAction
    data object ShareContact : ContactDetailsAction
}