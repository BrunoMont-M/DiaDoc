package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.example.diadoc.repository.CatalogoAlimentosRepository
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanNutricionalViewModel(
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository(),
    private val catalogoRepository: CatalogoAlimentosRepository = CatalogoAlimentosRepository(),
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository()
) : ViewModel() {

    private val _dietaState = MutableStateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?>(null)
    val dietaState: StateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?> = _dietaState

    private val _catalogoResultados = MutableStateFlow<List<Alimento>>(emptyList())
    val catalogoResultados: StateFlow<List<Alimento>> = _catalogoResultados

    private val _alertaRestriccion = MutableStateFlow<String?>(null)
    val alertaRestriccion: StateFlow<String?> = _alertaRestriccion

    fun cargarDietaDeHoy(codUsuario: String) {
        viewModelScope.launch {
            _dietaState.value = Resource.Loading
            try {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val plan = planRepository.obtenerPlanDeHoy(codUsuario, fechaHoy)

                if (plan != null) {
                    val dieta = dietaRepository.obtenerDietaPorPlan(plan.codPlan)
                    if (dieta != null) {
                        val menuCompleto = dietaRepository.obtenerMenuCompleto(dieta.codDieta)
                        _dietaState.value = Resource.Success(Pair(dieta, menuCompleto))
                    } else {
                        _dietaState.value = Resource.Error("El plan existe pero no tiene una dieta asignada.")
                    }
                } else {
                    _dietaState.value = Resource.Error("Aún no has generado tu plan de hoy.")
                }
            } catch (e: Exception) {
                _dietaState.value = Resource.Error("Error al cargar la dieta.")
            }
        }
    }

    fun alternarConsumoComida(codPlan: String, codDieta: String, uid: String, codDetDieta: String, consumidoActual: Boolean) {
        viewModelScope.launch {
            val nuevoEstado = !consumidoActual
            val exito = dietaRepository.marcarComidaComoConsumida(codDieta, codDetDieta, nuevoEstado)
            if (exito) {
                val menuCompleto = dietaRepository.obtenerMenuCompleto(codDieta)
                val totalComidas = menuCompleto.size
                val comidasConsumidas = menuCompleto.keys.count { it.consumido }
                val nuevoProgreso = if (totalComidas > 0) comidasConsumidas.toDouble() / totalComidas.toDouble() else 0.0
                planRepository.actualizarProgresoDieta(codPlan, nuevoProgreso)
                cargarDietaDeHoy(uid)
            }
        }
    }

    fun buscarEnCatalogo(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _catalogoResultados.value = catalogoRepository.buscarAlimentos(query)
            } else {
                _catalogoResultados.value = emptyList()
            }
        }
    }

    fun limpiarCatalogo() { _catalogoResultados.value = emptyList() }
    fun limpiarAlerta() { _alertaRestriccion.value = null }

    fun agregarAlimento(codDieta: String, uid: String, codDetDieta: String, alimento: Alimento) {
        viewModelScope.launch {
            val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
            if (perfil != null) {
                val restriccionesUsuario = perfilRepository.obtenerRestriccionesDelPerfil(perfil.codPerfil)
                val conflicto = alimento.alergenos.firstOrNull { restriccionesUsuario.contains(it) }

                if (conflicto != null) {
                    _alertaRestriccion.value = "Bloqueado: Este alimento contiene ingredientes ($conflicto) prohibidos por tu perfil médico."
                    return@launch
                }
            }

            val exito = dietaRepository.agregarAlimentoAComida(codDieta, codDetDieta, alimento)
            if (exito) {
                cargarDietaDeHoy(uid)
            }
        }
    }

    fun eliminarAlimento(codDieta: String, uid: String, codDetDieta: String, codAlimento: String) {
        viewModelScope.launch {
            val exito = dietaRepository.eliminarAlimentoDeComida(codDieta, codDetDieta, codAlimento)
            if (exito) {
                cargarDietaDeHoy(uid)
            }
        }
    }
}