package com.example.diadoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.BuildConfig
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.UsuarioRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private val _rachaActual = MutableStateFlow(0)
    val rachaActual: StateFlow<Int> = _rachaActual

    private val _tipDelDia = MutableStateFlow<String?>(null)
    val tipDelDia: StateFlow<String?> = _tipDelDia

    private val _historialMetricas = MutableStateFlow<List<Float>>(emptyList())
    val historialMetricas: StateFlow<List<Float>> = _historialMetricas

    private val _alertaContextual = MutableStateFlow<String?>(null)
    val alertaContextual: StateFlow<String?> = _alertaContextual

    private val _comparativaSemanal = MutableStateFlow<String>("")
    val comparativaSemanal: StateFlow<String> = _comparativaSemanal

    private var tipsMensualesCache: List<String> = emptyList()

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
                _historialMetricas.value = listOf(118f, 110f, 102f, 98f, 105f)
                _comparativaSemanal.value = "Esta semana tu glucosa promedio en ayunas fue de 106 mg/dL. Esto representa una mejora clínica del 4.5% respecto a tu promedio de la semana pasada (111 mg/dL)."
            } else if (patologiasLocales.contains("sarcopenia") || patologiasLocales.contains("desnutrición")) {
                _metricaDinamica.value = listOf("Objetivo Proteico", "65", "gramos", "Te faltan 25g para tu meta de retención.")
                _historialMetricas.value = listOf(50f, 55f, 60f, 62f, 65f)
                _comparativaSemanal.value = "Tu retención de masa muscular se mantiene estable. Promediaste 60g de proteína diaria, cumpliendo con el umbral anabólico un 12% más que la semana anterior."
            } else {
                _metricaDinamica.value = listOf("Calorías Activas", "1240", "kcal", "¡Excelente ritmo! En déficit calórico.")
                _historialMetricas.value = listOf(1100f, 1150f, 1200f, 1180f, 1240f)
                _comparativaSemanal.value = "Esta semana promediaste 1184 kcal quemadas, manteniendo un déficit calórico constante. ¡Incrementaste un 5% tu tasa de actividad basal!"
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

            evaluarAlertasContextuales(plan, _vasosAgua.value)
            val historialPlanes = planRepository.obtenerPlanesPorUsuario(uid)
            calcularRacha(historialPlanes, fechaHoy)
            procesarTipMensualConIA(uid, patologiasLocales.ifBlank { "Ninguna" })

        } catch (e: Exception) {
            Log.e("Dashboard", "Error cargando dashboard: ${e.message}")
        }
    }

    private fun evaluarAlertasContextuales(plan: PlanDiario?, vasos: Int) {
        if (plan == null) {
            _alertaContextual.value = "⚠️ No has generado tu plan de hoy. La IA nutricional te espera."
        } else if (vasos == 0) {
            _alertaContextual.value = "💧 Alerta de hidratación: El agua es vital para tu metabolismo. ¡Registra tu primer vaso!"
        } else {
            _alertaContextual.value = null
        }
    }

    private fun calcularRacha(planes: List<PlanDiario>, fechaHoyStr: String) {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val planesOrdenados = planes.mapNotNull {
            val date = format.parse(it.fechaInicio)
            if (date != null) Pair(date, it) else null
        }.sortedByDescending { it.first }

        var racha = 0
        val cal = Calendar.getInstance()
        cal.time = Date()

        val planHoy = planesOrdenados.find { format.format(it.first) == fechaHoyStr }?.second
        if (planHoy != null && planHoy.porcentCumplimiento >= 0.99) {
            racha++
        }

        cal.add(Calendar.DAY_OF_YEAR, -1)
        var diaAnteriorStr = format.format(cal.time)

        for (par in planesOrdenados) {
            val fechaPlan = format.format(par.first)
            if (fechaPlan == fechaHoyStr) continue

            if (fechaPlan == diaAnteriorStr) {
                if (par.second.porcentCumplimiento >= 0.99) {
                    racha++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    diaAnteriorStr = format.format(cal.time)
                } else {
                    break
                }
            } else if (par.first.before(cal.time)) {
                break
            }
        }
        _rachaActual.value = racha
    }

    private suspend fun procesarTipMensualConIA(uid: String, patologias: String) {
        if (tipsMensualesCache.isNotEmpty()) return

        val db = FirebaseFirestore.getInstance()
        val cal = Calendar.getInstance()
        val mesAnio = SimpleDateFormat("MM_yyyy", Locale.getDefault()).format(cal.time)
        val docId = "${uid}_${mesAnio}"

        try {
            val doc = db.collection("tips_mensuales").document(docId).get().await()
            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                val tips = doc.get("tips") as? List<String>
                if (!tips.isNullOrEmpty()) {
                    tipsMensualesCache = tips
                    mostrarTipAleatorio()
                }
            } else {
                _tipDelDia.value = "Generando tu cápsula educativa del mes..."
                val generativeModel = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)
                val prompt = """
                    Genera un JSON con un array de 30 tips médicos y nutricionales muy cortos (máximo 15 palabras por tip).
                    Deben ser variados y estar estrictamente adaptados para un paciente con estas patologías: $patologias.
                    Estructura exacta: { "tips": ["tip 1", "tip 2"] }
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                var jsonText = response.text ?: ""
                val startIndex = jsonText.indexOf("{")
                val endIndex = jsonText.lastIndexOf("}")
                if (startIndex != -1 && endIndex != -1) {
                    jsonText = jsonText.substring(startIndex, endIndex + 1)
                }

                val jsonObject = JSONObject(jsonText)
                val tipsArray = jsonObject.getJSONArray("tips")
                val tipsList = mutableListOf<String>()
                for (i in 0 until tipsArray.length()) {
                    tipsList.add(tipsArray.getString(i))
                }

                db.collection("tips_mensuales").document(docId).set(mapOf("tips" to tipsList)).await()
                tipsMensualesCache = tipsList
                mostrarTipAleatorio()
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error al procesar tip: ${e.message}")
            _tipDelDia.value = "El descanso adecuado es fundamental para la recuperación metabólica."
        }
    }

    fun mostrarTipAleatorio() {
        if (tipsMensualesCache.isNotEmpty()) {
            _tipDelDia.value = tipsMensualesCache.random()
        }
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
                evaluarAlertasContextuales(planActual, nuevoValor)
            }
        }
    }
}