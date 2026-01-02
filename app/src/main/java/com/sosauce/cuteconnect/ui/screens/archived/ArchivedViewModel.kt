package com.sosauce.cuteconnect.ui.screens.archived

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.data.datastore.getArchivedConversations
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.repository.ArchivedThreadsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArchivedViewModel(
    private val application: Application,
    private val archivedThreadsRepository: ArchivedThreadsRepository
): AndroidViewModel(application) {

    private val _state = MutableStateFlow(ArchivedState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getArchivedConversations(application).collectLatest { archivedThreads ->
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