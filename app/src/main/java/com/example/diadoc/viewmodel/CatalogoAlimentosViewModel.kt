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

    private var listaOriginal: List<Alimento> = emptyList()

    init {
        cargarAlimentos()
    }

    fun cargarAlimentos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Llama al método suspendido del repositorio real
                val listaDesdeFirebase = repository.buscarAlimentos("")
                listaOriginal = listaDesdeFirebase
                _alimentos.value = listaOriginal
            } catch (e: Exception) {
                _alimentos.value = emptyList()
                listaOriginal = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscarAlimentos(query: String) {
        if (query.isBlank()) {
            _alimentos.value = listaOriginal
        } else {
            val queryLower = query.lowercase().trim()
            _alimentos.value = listaOriginal.filter { alimento ->
                alimento.nombreAlimento.lowercase().contains(queryLower)
            }
        }
    }
}