package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.repository.CatalogoAlimentosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Si el proyecto usa algún modelo de datos específico como "Alimento", acordate de importarlo si te lo pide.

class CatalogoAlimentosViewModel(
    private val repository: CatalogoAlimentosRepository = CatalogoAlimentosRepository()
) : ViewModel() {

    // Estado para manejar la lista que viene de Firebase (por ahora vacía como simulación)
    private val _alimentos = MutableStateFlow<List<String>>(emptyList())
    val alimentos: StateFlow<List<String>> = _alimentos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarAlimentos()
    }

    fun cargarAlimentos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Ponemos estos tres de prueba locales para testear el diseño fluyendo desde el ViewModel
                _alimentos.value = listOf("Manzana Verde", "Galletas de Arroz", "Yogur Natural")
            } catch (e: Exception) {
                // Manejo de errores
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funciones preparadas para el ABM (US 14)
    fun agregarAlimento(nombre: String, carbohidratos: Double, calorias: Double, porcion: String) {
        viewModelScope.launch {
            // repository.agregarAlimento(...)
        }
    }

    fun eliminarAlimento(idAlimento: String) {
        viewModelScope.launch {
            // repository.eliminarAlimento(idAlimento)
        }
    }
}