package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.example.diadoc.repository.DietaRepository
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
    private val dietaRepository: DietaRepository = DietaRepository()
) : ViewModel() {

    private val _dietaState = MutableStateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?>(null)
    val dietaState: StateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?> = _dietaState

    fun cargarDietaDeHoy(codUsuario: String) {
        viewModelScope.launch {
            _dietaState.value = Resource.Loading
            try {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                // PASO 1: Buscar el Plan Diario del Usuario
                val plan = planRepository.obtenerPlanDeHoy(codUsuario, fechaHoy)

                if (plan != null) {
                    // PASO 2: Con el codPlan, buscar la Dieta asociada
                    val dieta = dietaRepository.obtenerDietaPorPlan(plan.codPlan)

                    if (dieta != null) {
                        // PASO 3: Traer los detalles y alimentos
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
}