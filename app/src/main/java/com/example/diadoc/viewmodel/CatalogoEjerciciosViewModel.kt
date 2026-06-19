package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.repository.EjercicioRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CatalogoEjerciciosViewModel(
    private val repository: EjercicioRepository = EjercicioRepository()
) : ViewModel() {

    private val _ejercicios = MutableStateFlow<List<Ejercicio>>(emptyList())
    val ejercicios: StateFlow<List<Ejercicio>> = _ejercicios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarEjercicios()
    }

    fun cargarEjercicios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listaDesdeFirebase = repository.obtenerTodosLosEjercicios()
                _ejercicios.value = listaDesdeFirebase
            } catch (e: Exception) {
                _ejercicios.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarOActualizarEjercicio(ejercicio: Ejercicio) {
        viewModelScope.launch {
            _isLoading.value = true
            val exito = repository.guardarEjercicio(ejercicio)
            if (exito) {
                cargarEjercicios()
            } else {
                _isLoading.value = false
            }
        }
    }

    fun eliminarEjercicio(codEjercicio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirebaseFirestore.getInstance()
                    .collection("ejercicios")
                    .document(codEjercicio)
                    .delete()
                    .await()
                cargarEjercicios()
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}