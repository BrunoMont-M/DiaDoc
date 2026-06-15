package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.repository.ControlDiarioRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReporteProgresoViewModel(
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val controlRepository: ControlDiarioRepository = ControlDiarioRepository(),
    private val rutinaRepository: RutinaRepository = RutinaRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Datos procesados listos para la UI
    private val _tendenciaGlucosa = MutableStateFlow<List<Float>>(emptyList())
    val tendenciaGlucosa: StateFlow<List<Float>> = _tendenciaGlucosa

    private val _promedioGlucosa = MutableStateFlow(0f)
    val promedioGlucosa: StateFlow<Float> = _promedioGlucosa

    private val _adherenciaDieta = MutableStateFlow(0f)
    val adherenciaDieta: StateFlow<Float> = _adherenciaDieta

    private val _adherenciaEjercicio = MutableStateFlow(0f)
    val adherenciaEjercicio: StateFlow<Float> = _adherenciaEjercicio

    private val _totalRegistros = MutableStateFlow(0)
    val totalRegistros: StateFlow<Int> = _totalRegistros

    fun generarReporte(uid: String, fechaDesdeMilis: Long?, fechaHastaMilis: Long?) {
        // 1. REGLAS DE NEGOCIO Y VALIDACIÓN
        if (fechaDesdeMilis == null || fechaHastaMilis == null) {
            _error.value = "Debes seleccionar ambas fechas."
            return
        }

        if (fechaDesdeMilis > fechaHastaMilis) {
            _error.value = "La fecha de inicio no puede ser posterior a la fecha de fin."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Ajuste para incluir todo el último día hasta las 23:59:59
                val finDelDiaMilis = fechaHastaMilis + 86399999L

                // 2. FILTRAR CONTROLES (Glucosa)
                val controles = controlRepository.obtenerControlesPorUsuario(uid)
                val formatCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                val glucosasEnRango = mutableListOf<Float>()
                for (control in controles) {
                    try {
                        val fechaControl = formatCompleto.parse(control.fechaHoraControl)
                        if (fechaControl != null && fechaControl.time in fechaDesdeMilis..finDelDiaMilis) {
                            val detalles = controlRepository.obtenerDetallesDeControl(control.codControl)
                            // Buscamos específicamente si el paciente registró Glucosa
                            val biometria = detalles.find { it.tipoMedicacion.startsWith("Biometría") || it.tipoMedicacion.contains("Glucosa") }
                            if (biometria != null) {
                                glucosasEnRango.add(biometria.valorNumerico.toFloat())
                            }
                        }
                    } catch (e: Exception) { continue }
                }

                // Invertimos la lista para que el gráfico vaya de más antiguo a más reciente
                _tendenciaGlucosa.value = glucosasEnRango.reversed()
                _promedioGlucosa.value = if (glucosasEnRango.isNotEmpty()) glucosasEnRango.average().toFloat() else 0f
                _totalRegistros.value = glucosasEnRango.size

                // 3. FILTRAR PLANES Y RUTINAS (Adherencia a Dieta y Ejercicio)
                val planes = planRepository.obtenerPlanesPorUsuario(uid)
                val formatCorto = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                var sumaDieta = 0f
                var sumaEjercicio = 0f
                var planesValidosDieta = 0
                var planesValidosEjercicio = 0

                for (plan in planes) {
                    try {
                        val fechaPlan = formatCorto.parse(plan.fechaInicio)
                        if (fechaPlan != null && fechaPlan.time in fechaDesdeMilis..finDelDiaMilis) {

                            // Adherencia Nutricional
                            sumaDieta += plan.porcentCumplimiento.toFloat()
                            planesValidosDieta++

                            // Adherencia Física (Buscamos la rutina de ese plan)
                            val rutina = rutinaRepository.obtenerRutinaPorPlan(plan.codPlan)
                            if (rutina != null) {
                                val detallesRutina = rutinaRepository.obtenerDetallesDeRutina(rutina.codRutina)
                                if (detallesRutina.isNotEmpty()) {
                                    val completados = detallesRutina.count { it.consumido }
                                    sumaEjercicio += (completados.toFloat() / detallesRutina.size)
                                    planesValidosEjercicio++
                                }
                            }
                        }
                    } catch (e: Exception) { continue }
                }

                _adherenciaDieta.value = if (planesValidosDieta > 0) sumaDieta / planesValidosDieta else 0f
                _adherenciaEjercicio.value = if (planesValidosEjercicio > 0) sumaEjercicio / planesValidosEjercicio else 0f

            } catch (e: Exception) {
                _error.value = "Error de conexión al procesar los datos del reporte."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarError() { _error.value = null }
}