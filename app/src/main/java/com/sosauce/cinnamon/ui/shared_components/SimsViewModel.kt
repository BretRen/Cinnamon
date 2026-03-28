package com.sosauce.cinnamon.ui.shared_components

import androidx.lifecycle.ViewModel
import com.sosauce.cinnamon.domain.repository.SimsRepository

class SimsViewModel(
    private val simsRepository: SimsRepository
): ViewModel() {

    fun fetchSims() = simsRepository.fetchSims()
}