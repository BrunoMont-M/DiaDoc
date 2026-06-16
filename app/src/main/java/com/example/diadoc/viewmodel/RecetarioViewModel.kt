package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.RecetaPersonalizada
import com.example.diadoc.repository.RecetaPersonalizadaRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class RecetarioViewModel(
    private val repository: RecetaPersonalizadaRepository = RecetaPersonalizadaRepository()
) : ViewModel() {

    private val _recetas = MutableStateFlow<List<RecetaPersonalizada>>(emptyList())
    val recetas: StateFlow<List<RecetaPersonalizada>> = _recetas

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var cacheRecetasCompleto: List<RecetaPersonalizada> = emptyList()

    fun cargarRecetas(codUsuario: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = repository.obtenerRecetas(
                    codUsuario = codUsuario,
                    tipoComida = if (_categoriaSeleccionada.value == "Todas") null else _categoriaSeleccionada.value
                )
                cacheRecetasCompleto = resultado
                aplicarFiltroBusqueda()
            } catch (e: Exception) {
                _recetas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cambiarCategoria(categoria: String, codUsuario: String) {
        _categoriaSeleccionada.value = categoria
        cargarRecetas(codUsuario)
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        aplicarFiltroBusqueda()
    }

    private fun aplicarFiltroBusqueda() {
        val query = _searchQuery.value.trim().lowercase()
        if (query.isEmpty()) {
            _recetas.value = cacheRecetasCompleto
        } else {
            _recetas.value = cacheRecetasCompleto.filter {
                it.nombreReceta.lowercase().contains(query) ||
                        it.instruccionesReceta.lowercase().contains(query)
            }
        }
    }

    fun alternarFavorito(receta: RecetaPersonalizada, codUsuario: String) {
        viewModelScope.launch {
            val exito = repository.alternarFavorito(receta.codReceta, receta.esFavorita)
            if (exito) {
                cargarRecetas(codUsuario)
            }
        }
    }

    fun guardarRecetaDesdeIA(detalle: DetalleDieta, codUsuario: String, guardadoDefinitivo: Boolean) {
        viewModelScope.launch {
            val fechaExp = siAplicaTTL(guardadoDefinitivo)

            val nuevaReceta = RecetaPersonalizada(
                codUsuario = codUsuario,
                nombreReceta = detalle.nombrePlato,
                instruccionesReceta = detalle.descripcionPlato + "\n\nPasos:\n" + detalle.preparacion.joinToString("\n"),
                tipoComida = detalle.tipoComida,
                origenIA = true,
                guardadaDefinitiva = guardadoDefinitivo,
                fechaExpiracion = fechaExp,
                kcalTotales = detalle.kcalTotales,
                carbohidratosTotales = detalle.carbohidratosTotales
            )
            repository.guardarReceta(nuevaReceta)
        }
    }

    private fun siAplicaTTL(guardadoDefinitivo: Boolean): Timestamp? {
        if (guardadoDefinitivo) return null
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        return Timestamp(calendar.time)
    }
}