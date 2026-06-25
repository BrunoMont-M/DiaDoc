package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Alimento
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
            } catch (_: Exception) {
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

    fun guardarRecetaDesdeIA(
        detalle: DetalleDieta,
        alimentos: List<Alimento>,
        codUsuario: String,
        guardadoDefinitivo: Boolean
    ) {
        viewModelScope.launch {
            val fechaExp = siAplicaTTL(guardadoDefinitivo)

            val kcalCalc = if (alimentos.isNotEmpty()) alimentos.sumOf { it.kcalBase } else detalle.kcalTotales
            val protCalc = if (alimentos.isNotEmpty()) alimentos.sumOf { it.proteinasBase } else 0.0
            val carbCalc = if (alimentos.isNotEmpty()) alimentos.sumOf { it.carbohidratosBase } else detalle.carbohidratosTotales

            val ingredientesStr = alimentos.joinToString("@@") { "${it.nombreAlimento}::${it.kcalBase.toInt()}" }
            val pasosStr = detalle.preparacion.joinToString("@@")
            val instruccionesEstructuradas = "${detalle.descripcionPlato}|||${ingredientesStr}|||${pasosStr}"

            val nuevaReceta = RecetaPersonalizada(
                codUsuario = codUsuario,
                nombreReceta = detalle.nombrePlato,
                instruccionesReceta = instruccionesEstructuradas,
                tipoComida = detalle.tipoComida,
                origenIA = true,
                guardadaDefinitiva = guardadoDefinitivo,
                fechaExpiracion = fechaExp,
                kcalTotales = kcalCalc,
                proteinasTotales = protCalc,
                carbohidratosTotales = carbCalc
            )
            repository.guardarReceta(nuevaReceta)
            cargarRecetas(codUsuario)
        }
    }

    fun guardarRecetaManual(
        codUsuario: String,
        nombre: String,
        instrucciones: String,
        kcal: Double,
        prot: Double,
        carb: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val nuevaReceta = RecetaPersonalizada(
                codReceta = "",
                codUsuario = codUsuario,
                nombreReceta = nombre,
                instruccionesReceta = instrucciones,
                tipoComida = "Personalizada",
                origenIA = false,
                guardadaDefinitiva = true,
                fechaExpiracion = null,
                kcalTotales = kcal,
                proteinasTotales = prot,
                carbohidratosTotales = carb
            )

            val exito = repository.guardarReceta(nuevaReceta)
            if (exito) {
                cargarRecetas(codUsuario)
                onSuccess()
            } else {
                _isLoading.value = false
            }
        }
    }

    fun eliminarReceta(codReceta: String, codUsuario: String) {
        viewModelScope.launch {
            val exito = repository.eliminarReceta(codReceta)
            if (exito) {
                cargarRecetas(codUsuario)
            }
        }
    }

    private fun siAplicaTTL(guardadoDefinitivo: Boolean): Timestamp? {
        if (guardadoDefinitivo) return null // Las manuales o favoritas JAMÁS se borran

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7) // Cambiado a 7 días de auto-destrucción
        return Timestamp(calendar.time)
    }
}