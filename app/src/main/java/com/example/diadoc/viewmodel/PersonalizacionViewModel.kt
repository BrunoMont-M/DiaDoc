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

class PersonalizacionViewModel(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val temaApp: StateFlow<Int> = preferenciasRepository.temaAppFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, 0
    )

    val paletaApp: StateFlow<Int> = preferenciasRepository.paletaAppFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, 0
    )

    fun cambiarTema(nuevoTema: Int) {
        viewModelScope.launch {
            preferenciasRepository.setTemaApp(nuevoTema)
        }
    }

    fun cambiarPaleta(nuevaPaleta: Int) {
        viewModelScope.launch {
            preferenciasRepository.setPaletaApp(nuevaPaleta)
        }
    }
}

class PersonalizacionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalizacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalizacionViewModel(PreferenciasRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}