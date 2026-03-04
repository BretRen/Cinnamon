package com.sosauce.cuteconnect.ui.shared_components

import androidx.lifecycle.ViewModel
import com.sosauce.cuteconnect.domain.repository.SimsRepository

class SimsViewModel(
    private val simsRepository: SimsRepository
): ViewModel() {

    fun fetchSims() = simsRepository.fetchSims()
}