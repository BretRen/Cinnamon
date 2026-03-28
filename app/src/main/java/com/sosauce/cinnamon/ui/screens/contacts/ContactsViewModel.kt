package com.sosauce.cinnamon.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.domain.repository.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactsRepository: ContactsRepository
): ViewModel() {

    private val _state = MutableStateFlow(ContactsState(isLoading = true))
    val state = _state.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactsRepository.fetchContacts()
            _state.update {
                it.copy(
                    isLoading = false,
                    contacts = contacts
                )
            }
        }
    }

}

data class ContactsState(
    val isLoading: Boolean = false,
    val contacts: List<CuteContact> = emptyList()
)