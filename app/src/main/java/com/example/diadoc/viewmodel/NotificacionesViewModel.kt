package com.example.diadoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diadoc.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificacionesViewModel(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val notifComidas: StateFlow<Boolean> = preferenciasRepository.notifComidasFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, true
    )
    val notifRutinas: StateFlow<Boolean> = preferenciasRepository.notifRutinasFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, true
    )
    val notifSistema: StateFlow<Boolean> = preferenciasRepository.notifSistemaFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, true
    )

    fun toggleNotifComidas(activo: Boolean) {
        viewModelScope.launch { preferenciasRepository.setNotifComidas(activo) }
    }

    fun toggleNotifRutinas(activo: Boolean) {
        viewModelScope.launch { preferenciasRepository.setNotifRutinas(activo) }
    }

    fun toggleNotifSistema(activo: Boolean) {
        viewModelScope.launch { preferenciasRepository.setNotifSistema(activo) }
    }
}

class NotificacionesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificacionesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificacionesViewModel(PreferenciasRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}