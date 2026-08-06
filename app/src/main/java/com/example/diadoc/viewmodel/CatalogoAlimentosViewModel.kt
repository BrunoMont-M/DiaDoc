package com.example.diadoc.viewmodel

import android.graphics.Bitmap
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

    private val _alimentoIA = MutableStateFlow<Alimento?>(null)
    val alimentoIA: StateFlow<Alimento?> = _alimentoIA.asStateFlow()

    private var listaOriginal: List<Alimento> = emptyList()

    init {
        cargarAlimentos()
    }

    fun cargarAlimentos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listaDesdeFirebase = repository.buscarAlimentos("")
                listaOriginal = listaDesdeFirebase
                _alimentos.value = listaOriginal.reversed().take(10)
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
            _alimentos.value = listaOriginal.reversed().take(10)
        } else {
            val queryLower = query.lowercase().trim()
            _alimentos.value = listaOriginal.filter { alimento ->
                alimento.nombreAlimento.lowercase().contains(queryLower)
            }
        }
    }

    // Se agregaron indiceGlucemico y alergenos a la firma de la función
    fun guardarAlimento(
        codAlimento: String?,
        nombre: String,
        kcal: Double,
        grasas: Double,
        carbohidratos: Double,
        proteinas: Double,
        indiceGlucemico: Int,
        alergenos: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val alimento = Alimento(
                    codAlimento = codAlimento ?: "",
                    nombreAlimento = nombre,
                    kcalBase = kcal,
                    grasasBase = grasas,
                    carbohidratosBase = carbohidratos,
                    proteinasBase = proteinas,
                    indiceGlucemico = indiceGlucemico,
                    alergenos = alergenos
                )
                repository.guardarAlimento(alimento)
                cargarAlimentos() // Refresca la lista después de guardar
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarAlimento(codAlimento: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.eliminarAlimento(codAlimento)
                cargarAlimentos()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analizarImagenConIA(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = repository.analizarAlimentoConIA(bitmap)
                _alimentoIA.value = resultado
            } catch (e: Exception) {
                _alimentoIA.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarAlimentoIA() {
        _alimentoIA.value = null
    }
}