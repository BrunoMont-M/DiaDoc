package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Alimento
import com.example.diadoc.repository.CatalogoAlimentosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogoAlimentosViewModel(
    private val repository: CatalogoAlimentosRepository = CatalogoAlimentosRepository()
) : ViewModel() {

    private val _alimentos = MutableStateFlow<List<Alimento>>(emptyList())
    val alimentos: StateFlow<List<Alimento>> = _alimentos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarAlimentos()
    }

    fun cargarAlimentos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Llama al método suspendido del repositorio real
                val listaDesdeFirebase = repository.buscarAlimentos("")
                _alimentos.value = listaDesdeFirebase
            } catch (e: Exception) {
                _alimentos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}