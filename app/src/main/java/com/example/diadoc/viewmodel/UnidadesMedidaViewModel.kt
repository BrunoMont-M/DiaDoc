package com.example.diadoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diadoc.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnidadesMedidaViewModel(private val repositorio: PreferenciasRepository) : ViewModel() {

    val usarMgdl = repositorio.usarMgdlFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val usarKg = repositorio.usarKgFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val usarCm = repositorio.usarCmFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val usarMl = repositorio.usarMlFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun actualizarMgdl(usarMgdl: Boolean) {
        viewModelScope.launch { repositorio.setUsarMgdl(usarMgdl) }
    }

    fun actualizarKg(usarKg: Boolean) {
        viewModelScope.launch { repositorio.setUsarKg(usarKg) }
    }

    fun actualizarCm(usarCm: Boolean) {
        viewModelScope.launch { repositorio.setUsarCm(usarCm) }
    }

    fun actualizarMl(usarMl: Boolean) {
        viewModelScope.launch { repositorio.setUsarMl(usarMl) }
    }
}

// Factory necesario para la instanciación manual
class UnidadesMedidaViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnidadesMedidaViewModel::class.java)) {
            val repositorio = PreferenciasRepository(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return UnidadesMedidaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}