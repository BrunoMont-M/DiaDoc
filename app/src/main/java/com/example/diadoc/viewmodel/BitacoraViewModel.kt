package com.example.diadoc.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.ControlDiario
import com.example.diadoc.model.DetalleControl
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.repository.ControlDiarioRepository
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RegistroHistorial(val hora: String, val descripcion: String)

class BitacoraViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val controlRepository: ControlDiarioRepository = ControlDiarioRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository()
) : ViewModel() {

    private val _patologias = MutableStateFlow<String>("")
    val patologias: StateFlow<String> = _patologias

    private val _progresoHoy = MutableStateFlow(0f)
    val progresoHoy: StateFlow<Float> = _progresoHoy

    private val _comidasCheckIn = MutableStateFlow<List<DetalleDieta>>(emptyList())
    val comidasCheckIn: StateFlow<List<DetalleDieta>> = _comidasCheckIn

    private val _historialReciente = MutableStateFlow<List<RegistroHistorial>>(emptyList())
    val historialReciente: StateFlow<List<RegistroHistorial>> = _historialReciente

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private var currentCodDieta: String = ""
    private var currentCodPlan: String = ""

    val usarMgdl: StateFlow<Boolean> = preferenciasRepository.usarMgdlFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val usarKg: StateFlow<Boolean> = preferenciasRepository.usarKgFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun cargarBitacora(uid: String) {
        viewModelScope.launch {
            try {
                val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
                if (perfil != null) {
                    val patologiasList = perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil)
                    val patologiasStr = patologiasList.joinToString(", ").lowercase()
                    _patologias.value = patologiasStr
                }

                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                val plan = planRepository.obtenerPlanDeHoy(uid, fechaHoy)
                if (plan != null) {
                    currentCodPlan = plan.codPlan
                    _progresoHoy.value = plan.porcentCumplimiento.toFloat()

                    val dieta = dietaRepository.obtenerDietaPorPlan(plan.codPlan)
                    if (dieta != null) {
                        currentCodDieta = dieta.codDieta
                        val menu = dietaRepository.obtenerMenuCompleto(dieta.codDieta)
                        val ordenCronologico = listOf("Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena", "Snack Media Tarde")

                        val comidasConMacros = menu.map { (detalle, alimentos) ->
                            val totalKcal = alimentos.sumOf { it.kcalBase.toDouble() }
                            val totalCarbs = alimentos.sumOf { it.carbohidratosBase.toDouble() }

                            detalle.copy(
                                kcalTotales = totalKcal,
                                carbohidratosTotales = totalCarbs
                            )
                        }.sortedBy { entrada ->
                            val index = ordenCronologico.indexOf(entrada.tipoComida)
                            if (index != -1) index else 99
                        }

                        _comidasCheckIn.value = comidasConMacros
                    }
                }

                cargarHistorial(uid, fechaHoy)

            } catch (e: Exception) {
                Log.e("BitacoraViewModel", "Error cargando bitácora: ${e.message}")
            }
        }
    }

    private suspend fun cargarHistorial(uid: String, fechaHoy: String) {
        val controles = controlRepository.obtenerControlesPorUsuario(uid)
        val formatCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formatHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        val historialDelDia = mutableListOf<RegistroHistorial>()

        val controlesHoy = controles.mapNotNull { control ->
            try {
                val date = formatCompleto.parse(control.fechaHoraControl)
                if (date != null && control.fechaHoraControl.startsWith(fechaHoy)) {
                    Pair(date, control)
                } else null
            } catch (e: Exception) { null }
        }.sortedByDescending { it.first }

        val isMgdl = usarMgdl.value
        val isKg = usarKg.value

        for ((date, control) in controlesHoy) {
            val horaStr = formatHora.format(date)
            val detalles = controlRepository.obtenerDetallesDeControl(control.codControl)
            val biometria = detalles.find { it.tipoMedicacion.startsWith("Biometría") }

            if (biometria != null) {
                val nombreMetrica = biometria.tipoMedicacion.replace("Biometría - ", "")
                val valorRaw = biometria.valorNumerico

                var valorConvertido = valorRaw
                var unidadFinal = biometria.unidadMedida // Por defecto (kg o mg/dL)

                // LÓGICA DE CONVERSIÓN (VÍA 1: LECTURA)
                if (nombreMetrica == "Glucosa" && !isMgdl) {
                    valorConvertido = valorRaw / 18.0
                    unidadFinal = "mmol/L"
                } else if (nombreMetrica == "Peso" && !isKg) {
                    valorConvertido = valorRaw * 2.20462
                    unidadFinal = "lb"
                }

                // Formateo visual
                val valorFormateado = if (unidadFinal == "mg/dL") {
                    valorConvertido.toInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", valorConvertido)
                }

                val momento = control.momentoDiaControl

                historialDelDia.add(RegistroHistorial(horaStr, "$nombreMetrica: $valorFormateado $unidadFinal ($momento)"))
            }
        }

        _historialReciente.value = historialDelDia
    }

    fun guardarMedicion(uid: String, valorStr: String, momento: String) {
        val valorIngresado = valorStr.toDoubleOrNull() ?: return
        viewModelScope.launch {
            _isSaving.value = true

            val isDiabetico = _patologias.value.contains("diabet")
            val isMgdl = usarMgdl.value
            val isKg = usarKg.value

            val tipo = if (isDiabetico) "Glucosa" else "Peso"
            val unidadBase = if (isDiabetico) "mg/dL" else "kg" // Firebase SIEMPRE guarda esto

            // LÓGICA DE CONVERSIÓN INVERSA (VÍA 2: ESCRITURA)
            var valorParaFirebase = valorIngresado
            if (isDiabetico && !isMgdl) {
                valorParaFirebase = valorIngresado * 18.0 // Usuario ingresó mmol/L, guardamos mg/dL
            } else if (!isDiabetico && !isKg) {
                valorParaFirebase = valorIngresado / 2.20462 // Usuario ingresó lb, guardamos kg
            }

            val fechaHoraActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val control = ControlDiario(
                codUsuario = uid,
                fechaHoraControl = fechaHoraActual,
                momentoDiaControl = momento,
                notasPaciente = "Registro manual en Bitácora"
            )

            val detalle = DetalleControl(
                tipoMedicacion = "Biometría - $tipo",
                unidadMedida = unidadBase, // Guardamos la unidad base estándar
                valorNumerico = valorParaFirebase // Guardamos el valor convertido a estándar
            )

            val exito = controlRepository.registrarControlCompleto(control, detalle)
            if (exito) {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                cargarHistorial(uid, fechaHoy)
            }
            _isSaving.value = false
        }
    }

    fun toggleComidaCheckIn(comidaId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val nuevoEstado = !currentStatus

            val updatedList = _comidasCheckIn.value.map { comida ->
                if (comida.codDetDieta == comidaId) comida.copy(consumido = nuevoEstado) else comida
            }
            _comidasCheckIn.value = updatedList

            val consumidas = updatedList.count { it.consumido }
            val nuevoPorcentaje = if (updatedList.isNotEmpty()) consumidas.toDouble() / updatedList.size else 0.0
            _progresoHoy.value = nuevoPorcentaje.toFloat()

            if (currentCodDieta.isNotEmpty() && currentCodPlan.isNotEmpty()) {
                dietaRepository.marcarComidaComoConsumida(currentCodDieta, comidaId, nuevoEstado)
                planRepository.actualizarProgresoDieta(currentCodPlan, nuevoPorcentaje)
            }
        }
    }
}

// Factory necesario para la instanciación con contexto
class BitacoraViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BitacoraViewModel::class.java)) {
            val repositorio = PreferenciasRepository(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return BitacoraViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}