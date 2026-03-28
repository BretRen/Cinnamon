package com.sosauce.cinnamon.ui.screens.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cinnamon.domain.model.CuteCallLog
import com.sosauce.cinnamon.domain.repository.DialerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DialerViewModel(
    private val dialerRepository: DialerRepository
): ViewModel() {

    private val _state = MutableStateFlow(DialerState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dialerRepository.fetchLatestCallLog()
                .collectLatest { logs ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            callLogs = logs
                        )
                    }
                }
        }
    }

    fun deleteCallLogs(ids: Collection<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { id ->
                dialerRepository.deleteCallLog(id)
            }
        }
    }

}

data class DialerState(
    val isLoading: Boolean = false,
    val callLogs: List<CuteCallLog> = emptyList()
)