package com.clawpet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawpet.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val repository: PetRepository,
) : ViewModel() {

    private val _petState = MutableStateFlow(PetState())
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePet().collect { _petState.value = it }
        }
    }

    fun feed() { viewModelScope.launch { repository.performAction(PetAction.FEED) } }
    fun pet() { viewModelScope.launch { repository.performAction(PetAction.PET) } }
    fun play() { viewModelScope.launch { repository.performAction(PetAction.PLAY) } }
    fun wake() { viewModelScope.launch { repository.performAction(PetAction.WAKE) } }
}