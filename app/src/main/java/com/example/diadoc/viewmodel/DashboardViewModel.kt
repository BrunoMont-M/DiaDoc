package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.UsuarioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository()
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _patologias = MutableStateFlow<String>("")
    val patologias: StateFlow<String> = _patologias

    private val _planHoy = MutableStateFlow<PlanDiario?>(null)
    val planHoy: StateFlow<PlanDiario?> = _planHoy

    private val _vasosAgua = MutableStateFlow(0)
    val vasosAgua: StateFlow<Int> = _vasosAgua

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _metricaDinamica = MutableStateFlow(listOf("Cargando...", "0", "", ""))
    val metricaDinamica: StateFlow<List<String>> = _metricaDinamica

    private val _comidasHoy = MutableStateFlow<List<DetalleDieta>>(emptyList())
    val comidasHoy: StateFlow<List<DetalleDieta>> = _comidasHoy

    fun cargarUsuario(uid: String) {
        viewModelScope.launch {
            _usuario.value = usuarioRepository.obtenerUsuario(uid)
            cargarDatosDashboard(uid)
        }
    }

    fun refrescarPantalla(uid: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            cargarDatosDashboard(uid)
            delay(500)
            _isRefreshing.value = false
        }
    }

    private suspend fun cargarDatosDashboard(uid: String) {
        try {
            val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
            var patologiasLocales = ""
            if (perfil != null) {
                val patologiasList = perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil)
                patologiasLocales = patologiasList.joinToString(", ").lowercase()
                _patologias.value = patologiasLocales
            }

            if (patologiasLocales.contains("diabet")) {
                _metricaDinamica.value = listOf("Última Glucosa", "105", "mg/dL", "Rango Saludable. Actualizado hoy.")
            } else if (patologiasLocales.contains("sarcopenia") || patologiasLocales.contains("desnutrición")) {
                _metricaDinamica.value = listOf("Objetivo Proteico", "65", "gramos", "Te faltan 25g para tu meta de retención.")
            } else {
                _metricaDinamica.value = listOf("Calorías Activas", "1240", "kcal", "¡Excelente ritmo! En déficit calórico.")
            }

            val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val plan = planRepository.obtenerPlanDeHoy(uid, fechaHoy)
            _planHoy.value = plan

            if (plan != null) {
                _vasosAgua.value = plan.vasosAgua

                val dieta = dietaRepository.obtenerDietaPorPlan(plan.codPlan)
                if (dieta != null) {
                    val menu = dietaRepository.obtenerMenuCompleto(dieta.codDieta)
                    val ordenCronologico = listOf("Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena", "Snack Media Tarde")
                    _comidasHoy.value = menu.keys.sortedBy { entrada ->
                        val index = ordenCronologico.indexOf(entrada.tipoComida)
                        if (index != -1) index else 99
                    }
                } else {
                    _comidasHoy.value = emptyList()
                }
            }
        } catch (e: Exception) { }
    }

    fun sumarVasoAgua() {
        val planActual = _planHoy.value
        val nuevoValor = _vasosAgua.value + 1
        if (nuevoValor <= 8) {
            _vasosAgua.value = nuevoValor
            if (planActual != null) {
                viewModelScope.launch {
                    planRepository.actualizarVasosAgua(planActual.codPlan, nuevoValor)
                }
            }
        }
    }
}