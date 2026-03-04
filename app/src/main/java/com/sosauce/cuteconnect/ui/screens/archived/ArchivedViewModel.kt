package com.sosauce.cuteconnect.ui.screens.archived

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.data.datastore.UserPreferences
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.repository.ArchivedThreadsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArchivedViewModel(
    private val archivedThreadsRepository: ArchivedThreadsRepository,
    private val userPreferences: UserPreferences
): ViewModel() {

    private val _state = MutableStateFlow(ArchivedState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.archivedConversations.collectLatest { archivedThreads ->
                val threads = archivedThreadsRepository.fetchArchivedThreads(archivedThreads)
                _state.update {
                    it.copy(
                        isLoading = false,
                        threads = threads
                    )
                }
            }
        }
    }

}

data class ArchivedState(
    val isLoading: Boolean = false,
    val threads: List<CuteConversation> = emptyList()
)