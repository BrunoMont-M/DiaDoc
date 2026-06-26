package com.example.diadoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.DetalleRutina
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.model.Rutina
import com.example.diadoc.repository.ConfiguracionRepository
import com.example.diadoc.repository.EjercicioRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.RutinaRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeneradorRutinaViewModel(
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val ejercicioRepository: EjercicioRepository = EjercicioRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val rutinaRepository: RutinaRepository = RutinaRepository(),
    private val configRepository: ConfiguracionRepository = ConfiguracionRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _rutinaActual = MutableStateFlow<Rutina?>(null)
    val rutinaActual: StateFlow<Rutina?> = _rutinaActual

    private val _detallesRutina = MutableStateFlow<List<DetalleRutina>>(emptyList())
    val detallesRutina: StateFlow<List<DetalleRutina>> = _detallesRutina

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _errorEdicion = MutableStateFlow<String?>(null)
    val errorEdicion: StateFlow<String?> = _errorEdicion

    fun limpiarErrorEdicion() {
        _errorEdicion.value = null
    }

    fun cargarRutinaDeHoy(uid: String) {
        viewModelScope.launch {
            try {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val plan = planRepository.obtenerPlanDeHoy(uid, fechaHoy)

                if (plan != null) {
                    val rutina = rutinaRepository.obtenerRutinaPorPlan(plan.codPlan)
                    if (rutina != null) {
                        _rutinaActual.value = rutina
                        _detallesRutina.value = rutinaRepository.obtenerDetallesDeRutina(rutina.codRutina)
                    } else {
                        _rutinaActual.value = null
                        _detallesRutina.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("GeneradorRutina", "Error cargando rutina: ${e.message}")
            }
        }
    }

    fun generarRutinaConIA(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 1. Descargamos la llave segura de Firestore
                val apiKey = configRepository.obtenerApiKeyGemini()
                if (apiKey.isEmpty()) {
                    _error.value = "Error de infraestructura: API Key no encontrada en la base de datos."
                    _isLoading.value = false
                    return@launch
                }

                // 2. Inicializamos el motor IA localmente
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )

                val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
                val patologias = if (perfil != null) {
                    perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil)
                } else emptyList()

                val catalogoEjercicios = ejercicioRepository.obtenerTodosLosEjercicios()

                if (catalogoEjercicios.isEmpty()) {
                    _error.value = "No hay ejercicios en el catálogo maestro."
                    _isLoading.value = false
                    return@launch
                }

                var contextoFisico = "Datos biométricos no disponibles"
                if (perfil != null && perfil.pesoActual > 0 && perfil.alturaPerfil > 0) {
                    val alturaMetros = perfil.alturaPerfil / 100.0
                    val imc = perfil.pesoActual / (alturaMetros * alturaMetros)
                    val clasificacionIMC = when {
                        imc < 18.5 -> "Bajo peso (Priorizar ganancia de masa muscular sin fatiga extrema)"
                        imc in 18.5..24.9 -> "Saludable (Mantenimiento y tonificación)"
                        imc in 25.0..29.9 -> "Sobrepeso (Priorizar quema calórica y sensibilidad a la insulina)"
                        else -> "Obesidad (Priorizar cardio de muy bajo impacto articular y quema calórica)"
                    }
                    contextoFisico = "Peso: ${perfil.pesoActual}kg, Altura: ${perfil.alturaPerfil}cm, IMC: ${String.format(Locale.US, "%.1f", imc)} ($clasificacionIMC)"
                }

                val catalogoString = catalogoEjercicios.joinToString("\n") {
                    "- ID: ${it.codEjercicio} | Nombre: ${it.nombreEjercicio} | Impacto: ${it.impactoMuscular} | Grupo: ${it.grupoMuscular}"
                }

                val patologiasString = if (patologias.isNotEmpty()) patologias.joinToString(", ") else "Ninguna"

                val prompt = """
                    Eres un Entrenador Físico Clínico de alto nivel. Debes generar una rutina de entrenamiento diaria realista, altamente efectiva y clínicamente segura para un paciente.
                    
                    CONTEXTO HOLÍSTICO DEL PACIENTE:
                    - Patologías Clínicas: $patologiasString
                    - Estado Físico Actual: $contextoFisico
                    
                    REGLAS DE DISEÑO DE LA RUTINA:
                    1. Selección Dinámica: Utiliza tu criterio profesional para decidir la cantidad ideal de ejercicios para una sesión completa (ni muy pocos que sea inútil, ni demasiados que cause sobreentrenamiento).
                    2. Volumen de Entrenamiento Realista: ¡No seas conservador en exceso! El entrenamiento debe ser un estímulo real. 
                       - Si recomiendas un ejercicio cardiovascular continuo (ej. caminata, trote), el tiempo debe ser efectivo (ej. 30 a 60 minutos continuos). En ese caso, establece "series" en 1 y "repeticiones" en 1, y pon el tiempo real en las observaciones.
                       - Si recomiendas ejercicios de fuerza/resistencia, usa rangos de hipertrofia o fuerza reales (ej. 3 a 5 series, con 8 a 15 repeticiones).
                    3. Equilibrio Metabólico: Adapta el objetivo basándote en su Estado Físico Actual (IMC) y Patologías. Combina fuerza y cardio inteligentemente.
                    4. Impacto Muscular y Articular: Ten EXTREMO CUIDADO de no recomendar ejercicios de "Alto Impacto" si las patologías o la obesidad del paciente lo contraindican.
                    5. Catálogo Cerrado: Elige los ejercicios EXCLUSIVAMENTE de esta lista:
                    $catalogoString
                    
                    Genera tu respuesta ÚNICAMENTE en este formato JSON exacto (sin bloques de código (```), sin markdown, solo el JSON puro):
                    {
                        "nombreRutina": "Nombre atractivo de la rutina (ej: Quema Metabólica y Fuerza Inferior)",
                        "intensidad": "Baja, Media o Alta",
                        "ejercicios": [
                            {
                                "codEjercicio": "ID_DEL_EJERCICIO_AQUI",
                                "series": 4,
                                "repeticiones": 12,
                                "tiempoDescanso": 60,
                                "observacionesIA": "Explicación técnica de por qué este volumen de trabajo ayuda a su objetivo de IMC y es seguro para su patología."
                            }
                        ]
                    }
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                var jsonText = response.text ?: ""
                jsonText = jsonText.replace("```json", "").replace("```", "").trim()

                val jsonObject = JSONObject(jsonText)
                val nombreRutinaIA = jsonObject.getString("nombreRutina")
                val intensidadIA = jsonObject.getString("intensidad")
                val arrayEjercicios = jsonObject.getJSONArray("ejercicios")

                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                var plan = planRepository.obtenerPlanDeHoy(uid, fechaHoy)

                if (plan == null) {
                    val nuevoPlan = PlanDiario(codUsuario = uid, fechaInicio = fechaHoy)
                    val codPlanNuevo = planRepository.guardarPlan(nuevoPlan) ?: ""
                    plan = nuevoPlan.copy(codPlan = codPlanNuevo)
                }

                val nuevaRutina = Rutina(
                    codPlan = plan.codPlan,
                    nombreRutina = nombreRutinaIA,
                    intensidad = intensidadIA,
                    versionMotorIA = "Generado por DiaDoc FitnessEngine v3.0 (Holístico)"
                )

                val detallesGenerados = mutableListOf<DetalleRutina>()
                for (i in 0 until arrayEjercicios.length()) {
                    val ejJson = arrayEjercicios.getJSONObject(i)
                    detallesGenerados.add(
                        DetalleRutina(
                            codEjercicio = ejJson.getString("codEjercicio"),
                            ordenDetalle = i + 1,
                            seriesDetalle = ejJson.getInt("series"),
                            repeticionesDetalle = ejJson.getInt("repeticiones"),
                            tiempoDescanso = ejJson.getInt("tiempoDescanso"),
                            observacionesIA = ejJson.getString("observacionesIA"),
                            consumido = false
                        )
                    )
                }

                rutinaRepository.eliminarRutinasDelPlan(plan.codPlan)

                val exitoId = rutinaRepository.guardarRutinaCompleta(nuevaRutina, detallesGenerados)
                if (exitoId != null) {
                    _rutinaActual.value = nuevaRutina.copy(codRutina = exitoId)
                    _detallesRutina.value = detallesGenerados
                } else {
                    _error.value = "Error al guardar la rutina en la base de datos."
                }

            } catch (e: Exception) {
                Log.e("GeneradorRutina", "Error en IA: ${e.message}")
                _error.value = "El servicio de rutinas no está disponible temporalmente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun marcarEjercicioCompletado(codRutina: String, codDetalle: String, estadoActual: Boolean) {
        viewModelScope.launch {
            val nuevoEstado = !estadoActual

            val updatedList = _detallesRutina.value.map {
                if (it.codDetalle == codDetalle) it.copy(consumido = nuevoEstado) else it
            }
            _detallesRutina.value = updatedList

            rutinaRepository.marcarEjercicioComoConsumido(codRutina, codDetalle, nuevoEstado)
        }
    }

    fun eliminarEjercicio(codRutina: String, codDetalle: String) {
        viewModelScope.launch {
            val exito = rutinaRepository.eliminarEjercicio(codRutina, codDetalle)
            if (exito) {
                _detallesRutina.value = _detallesRutina.value.filter { it.codDetalle != codDetalle }
            } else {
                _errorEdicion.value = "Error de conexión al intentar eliminar el ejercicio."
            }
        }
    }

    fun actualizarCargaEjercicio(codRutina: String, codDetalle: String, series: Int, repeticiones: Int, descanso: Int) {
        viewModelScope.launch {
            val exito = rutinaRepository.actualizarCargaEjercicio(codRutina, codDetalle, series, repeticiones, descanso)
            if (exito) {
                _detallesRutina.value = _detallesRutina.value.map {
                    if (it.codDetalle == codDetalle) it.copy(seriesDetalle = series, repeticionesDetalle = repeticiones, tiempoDescanso = descanso) else it
                }
            } else {
                _errorEdicion.value = "Error de conexión al actualizar la carga de entrenamiento."
            }
        }
    }

    fun agregarEjercicioManual(uid: String, codRutina: String, ejercicio: Ejercicio) {
        viewModelScope.launch {
            try {
                val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
                val patologiasNombres = if (perfil != null) {
                    perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil).map { it.lowercase() }
                } else emptyList()

                if (ejercicio.impactoMuscular.equals("Alto", ignoreCase = true) || ejercicio.impactoMuscular.equals("Alto Impacto", ignoreCase = true)) {
                    val patologiasRiesgo = listOf("lesión", "artrosis", "artritis", "hernia", "osteoporosis", "sarcopenia")
                    val coincidencia = patologiasNombres.firstOrNull { pat -> patologiasRiesgo.any { pat.contains(it) } }

                    if (coincidencia != null) {
                        _errorEdicion.value = "[!] Bloqueo de seguridad: El ejercicio '${ejercicio.nombreEjercicio}' es de ALTO IMPACTO y está contraindicado por tu diagnóstico de ${coincidencia.uppercase()}."
                        return@launch
                    }
                }

                val ordenNuevo = (_detallesRutina.value.maxOfOrNull { it.ordenDetalle } ?: 0) + 1
                val nuevoDetalle = DetalleRutina(
                    codEjercicio = ejercicio.codEjercicio,
                    ordenDetalle = ordenNuevo,
                    seriesDetalle = 3,
                    repeticionesDetalle = 12,
                    tiempoDescanso = 60,
                    observacionesIA = "Reemplazo elegido manualmente.",
                    consumido = false
                )

                val exito = rutinaRepository.agregarEjercicioNuevo(codRutina, nuevoDetalle)
                if (exito) {
                    _detallesRutina.value = rutinaRepository.obtenerDetallesDeRutina(codRutina)
                } else {
                    _errorEdicion.value = "Error al intentar guardar el nuevo ejercicio en la base de datos."
                }
            } catch (e: Exception) {
                _errorEdicion.value = "Ocurrió un error inesperado al procesar la validación clínica."
            }
        }
    }
}