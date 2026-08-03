package com.example.diadoc.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.DetalleRutina
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.ConfiguracionRepository
import com.example.diadoc.repository.ControlDiarioRepository
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.PreferenciasRepository
import com.example.diadoc.repository.RutinaRepository
import com.example.diadoc.repository.UsuarioRepository
import com.example.diadoc.utils.AlarmReceiver
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// DATA CLASS PARA LA AGENDA
data class AlarmaDiaria(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val hora: Int,
    val minutos: Int,
    val vibrar: Boolean,
    val repeticion: String, // Ej: "Una vez", "Todos los días", "Personalizado"
    val tonoUri: String? = null // NUEVO: URL del tono de la alarma
)

class DashboardViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository(),
    private val controlRepository: ControlDiarioRepository = ControlDiarioRepository(),
    private val rutinaRepository: RutinaRepository = RutinaRepository(),
    private val configRepository: ConfiguracionRepository = ConfiguracionRepository()
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

    private val _valorBiometriaAbsoluto = MutableStateFlow<Float?>(null)
    val valorBiometriaAbsoluto: StateFlow<Float?> = _valorBiometriaAbsoluto

    private val _comidasHoy = MutableStateFlow<List<DetalleDieta>>(emptyList())
    val comidasHoy: StateFlow<List<DetalleDieta>> = _comidasHoy

    private val _ejerciciosHoy = MutableStateFlow<List<DetalleRutina>>(emptyList())
    val ejerciciosHoy: StateFlow<List<DetalleRutina>> = _ejerciciosHoy

    private val _porcentajeEjercicio = MutableStateFlow(0f)
    val porcentajeEjercicio: StateFlow<Float> = _porcentajeEjercicio

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

    // ESTADO PARA LAS ALARMAS DE LA AGENDA
    private val _alarmasAgenda = MutableStateFlow<List<AlarmaDiaria>>(emptyList())
    val alarmasAgenda: StateFlow<List<AlarmaDiaria>> = _alarmasAgenda

    private var tipsMensualesCache: List<String> = emptyList()

    val usarMl: StateFlow<Boolean> = preferenciasRepository.usarMlFlow.stateIn(viewModelScope, SharingStarted.Lazily, true)

    private var isMgdl = true
    private var isKg = true

    private var ultimoValorBiometriaRaw: Float? = null
    private var historialBiometriaRaw: List<Float> = emptyList()
    private var tipoPatologiaActual: String = ""

    init {
        viewModelScope.launch {
            preferenciasRepository.usarMgdlFlow.collect { mgdl ->
                isMgdl = mgdl
                actualizarMetricaDinamica()
            }
        }
        viewModelScope.launch {
            preferenciasRepository.usarKgFlow.collect { kg ->
                isKg = kg
                actualizarMetricaDinamica()
            }
        }
        cargarAlarmas()
    }

    private fun cargarAlarmas() {
        // En el futuro esto se cargará desde Room o DataStore
        _alarmasAgenda.value = emptyList()
    }

    fun guardarAlarma(context: Context, titulo: String, hora: Int, minutos: Int, vibrar: Boolean, repeticion: String, tonoUri: String?) {
        val nuevaAlarma = AlarmaDiaria(
            titulo = titulo,
            hora = hora,
            minutos = minutos,
            vibrar = vibrar,
            repeticion = repeticion,
            tonoUri = tonoUri // NUEVO: Guardamos la URL del tono
        )
        val listaActualizada = _alarmasAgenda.value.toMutableList()
        listaActualizada.add(nuevaAlarma)
        listaActualizada.sortBy { it.hora * 60 + it.minutos }
        _alarmasAgenda.value = listaActualizada

        programarAlarmaEnAndroid(context, nuevaAlarma)
    }

    private fun programarAlarmaEnAndroid(context: Context, alarma: AlarmaDiaria) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Configuramos el Intent
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TITULO_ALARMA", alarma.titulo)
            putExtra("VIBRAR_ALARMA", alarma.vibrar)
            putExtra("TONO_URI", alarma.tonoUri) // NUEVO: Pasamos la URL al receiver
        }

        // 2. Generamos un RequestCode único y el PendingIntent como Broadcast
        val requestCode = (alarma.hora * 60) + alarma.minutos // ID único basado en la hora para evitar conflictos
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Calculamos la hora exacta
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarma.hora)
            set(Calendar.MINUTE, alarma.minutos)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si la hora ya pasó hoy, la programamos para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 4. Programamos usando el método correcto según la versión de Android
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Obligatorio para que suene con el celular en reposo/pantalla apagada
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("DashboardViewModel", "Alarma programada exitosamente para las ${alarma.hora}:${alarma.minutos}")
        } catch (e: SecurityException) {
            Log.e("DashboardViewModel", "Error: Permiso SCHEDULE_EXACT_ALARM denegado", e)
        }
    }

    fun eliminarAlarma(id: String) {
        val listaActualizada = _alarmasAgenda.value.filter { it.id != id }
        _alarmasAgenda.value = listaActualizada
    }

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

    fun cargarProgresoEjercicio(uid: String) {
        viewModelScope.launch {
            try {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val plan = planRepository.obtenerPlanDeHoy(uid, fechaHoy)

                if (plan != null) {
                    val rutina = rutinaRepository.obtenerRutinaPorPlan(plan.codPlan)
                    if (rutina != null) {
                        val detalles = rutinaRepository.obtenerDetallesDeRutina(rutina.codRutina)
                        _ejerciciosHoy.value = detalles

                        if (detalles.isNotEmpty()) {
                            val completados = detalles.count { it.consumido }
                            _porcentajeEjercicio.value = completados.toFloat() / detalles.size
                        } else {
                            _porcentajeEjercicio.value = 0f
                        }
                    } else {
                        _ejerciciosHoy.value = emptyList()
                        _porcentajeEjercicio.value = 0f
                    }
                }
            } catch (e: Exception) {
                _porcentajeEjercicio.value = 0f
            }
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

            cargarHistorialReal(uid, patologiasLocales)

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

    private suspend fun cargarHistorialReal(uid: String, patologiasLocales: String) {
        tipoPatologiaActual = patologiasLocales
        val controles = controlRepository.obtenerControlesPorUsuario(uid)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val controlesOrdenados = controles.mapNotNull {
            try { Pair(format.parse(it.fechaHoraControl), it) } catch(e:Exception) { null }
        }.sortedByDescending { it.first }.map { it.second }

        val ultimosValores = mutableListOf<Float>()
        var ultimoValorRegistrado = "0"

        for (c in controlesOrdenados) {
            if (ultimosValores.size >= 5) break
            val detalles = controlRepository.obtenerDetallesDeControl(c.codControl)
            val biometria = detalles.find { it.tipoMedicacion.startsWith("Biometría") }
            if (biometria != null) {
                ultimosValores.add(biometria.valorNumerico.toFloat())
                if (ultimosValores.size == 1) {
                    ultimoValorRegistrado = biometria.valorNumerico.toString()
                }
            }
        }

        historialBiometriaRaw = if (ultimosValores.isEmpty()) emptyList() else ultimosValores.reversed()
        ultimoValorBiometriaRaw = if (ultimosValores.isNotEmpty()) ultimoValorRegistrado.toFloatOrNull() else null

        _valorBiometriaAbsoluto.value = ultimoValorBiometriaRaw

        actualizarMetricaDinamica()
    }

    private fun actualizarMetricaDinamica() {
        val isDiabet = tipoPatologiaActual.contains("diabet")
        val isSarcopenia = tipoPatologiaActual.contains("sarcopenia") || tipoPatologiaActual.contains("desnutrición")
        val valorRaw = ultimoValorBiometriaRaw

        val factorConversionGrafico = when {
            isDiabet -> if (isMgdl) 1f else (1f / 18.0f)
            else -> if (isKg) 1f else 2.20462f
        }
        _historialMetricas.value = historialBiometriaRaw.map { it * factorConversionGrafico }

        if (isDiabet) {
            if (valorRaw == null) {
                _metricaDinamica.value = listOf("Última Glucosa", "S/D", if (isMgdl) "mg/dL" else "mmol/L", "Actualizado con tu último check-in.")
            } else {
                val valorFinal = if (isMgdl) valorRaw.toInt().toString() else String.format(Locale.US, "%.1f", valorRaw / 18.0)
                val unidad = if (isMgdl) "mg/dL" else "mmol/L"
                _metricaDinamica.value = listOf("Última Glucosa", valorFinal, unidad, "Actualizado con tu último check-in.")
            }
            _comparativaSemanal.value = "Análisis Clínico en proceso. Registra más métricas esta semana para calcular tu tendencia de glucosa."

        } else if (isSarcopenia) {
            if (valorRaw == null) {
                _metricaDinamica.value = listOf("Peso Registrado", "S/D", if (isKg) "kg" else "lb", "Mantén tu seguimiento de masa muscular.")
            } else {
                val valorFinal = if (isKg) valorRaw.toString() else String.format(Locale.US, "%.1f", valorRaw * 2.20462)
                val unidad = if (isKg) "kg" else "lb"
                _metricaDinamica.value = listOf("Peso Registrado", valorFinal, unidad, "Mantén tu seguimiento de masa muscular.")
            }
            _comparativaSemanal.value = "Análisis Clínico en proceso. Registra más métricas esta semana para evaluar tu retención de masa."

        } else {
            if (valorRaw == null) {
                _metricaDinamica.value = listOf("Peso de Control", "S/D", if (isKg) "kg" else "lb", "Monitoreo general de salud.")
            } else {
                val valorFinal = if (isKg) valorRaw.toString() else String.format(Locale.US, "%.1f", valorRaw * 2.20462)
                val unidad = if (isKg) "kg" else "lb"
                _metricaDinamica.value = listOf("Peso de Control", valorFinal, unidad, "Monitoreo general de salud.")
            }
            _comparativaSemanal.value = "Mantenimiento activo. Tu registro biométrico ayuda a la IA a ajustar tus planes."
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

                val apiKey = configRepository.obtenerApiKeyGemini()
                if (apiKey.isEmpty()) {
                    _tipDelDia.value = "La hidratación y el buen descanso son pilares fundamentales de la salud."
                    return
                }

                val generativeModel = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = apiKey)
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

    fun limpiarDatos() {
        _usuario.value = null
        _patologias.value = ""
        _planHoy.value = null
        _vasosAgua.value = 0
        _isRefreshing.value = false
        _metricaDinamica.value = listOf("Cargando...", "0", "", "")
        _valorBiometriaAbsoluto.value = null
        _comidasHoy.value = emptyList()
        _ejerciciosHoy.value = emptyList()
        _porcentajeEjercicio.value = 0f
        _rachaActual.value = 0
        _tipDelDia.value = null
        _historialMetricas.value = emptyList()
        _alertaContextual.value = null
        _comparativaSemanal.value = ""
        tipsMensualesCache = emptyList()
    }
}