package com.example.diadoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.BuildConfig
import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.repository.AlimentoRepository
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.utils.Resource
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeneradorPlanViewModel(
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository(),
    private val alimentoRepository: AlimentoRepository = AlimentoRepository()
) : ViewModel() {

    private val _generacionState = MutableStateFlow<Resource<Boolean>?>(null)
    val generacionState: StateFlow<Resource<Boolean>?> = _generacionState

    // Inicializamos el modelo de Gemini usando la versión Flash (gratuita)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun generarPlanParaUsuario(codUsuario: String) {
        viewModelScope.launch {
            _generacionState.value = Resource.Loading
            try {
                // 1. Obtener perfil, patologías y restricciones del usuario
                val perfil = perfilRepository.obtenerPerfilPorUsuario(codUsuario)
                if (perfil == null) {
                    _generacionState.value = Resource.Error("Perfil médico no encontrado.")
                    return@launch
                }

                val patologias = perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil).joinToString(", ")
                val restricciones = perfilRepository.obtenerRestriccionesDelPerfil(perfil.codPerfil).joinToString(", ")

                // 2. Construir el Prompt Estructurado para la IA
                val prompt = """
                    Actúa como un nutricionista clínico experto. Debes generar un plan de alimentación diario para un paciente con las siguientes características:
                    - Peso: ${perfil.pesoActual} kg
                    - Altura: ${perfil.alturaPerfil} cm
                    - Patologías confirmadas: $patologias
                    - Restricciones alimentarias: $restricciones
                    
                    Prioriza el uso de ingredientes nobles y de bajo impacto glucémico como avena, semillas de chía y abundantes vegetales frescos en las preparaciones. Si los requerimientos clínicos y de calorías lo permiten, puedes sugerir adaptaciones saludables de platillos como la humita o tortitas integrales para mantener la variedad y adherencia al plan.
                    
                    Tu respuesta DEBE ser ÚNICAMENTE un objeto JSON válido con la siguiente estructura, sin texto adicional ni formato Markdown:
                    {
                      "objetivoPlan": "Mantenimiento",
                      "kcalDieta": 2000,
                      "nombreDieta": "Plan Saludable Día 1",
                      "comidas": [
                        {
                          "tipoComida": "Desayuno",
                          "alimentos": [
                            { "nombreAlimento": "Avena cocida", "kcalBase": 150.0, "proteinasBase": 5.0, "carbohidratosBase": 27.0, "grasasBase": 3.0, "indiceGlucemico": 55 }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                // 3. Llamar a Gemini limpiando cualquier formato residual usando Unicode para evitar cortes de interfaz
                val response = generativeModel.generateContent(prompt)
                val jsonText = response.text?.replace("\u0060\u0060\u0060json", "")?.replace("\u0060\u0060\u0060", "")?.trim() ?: ""

                // 4. Parsear el JSON y guardar en la Base de Datos
                procesarYGuardarJSON(jsonText, codUsuario)

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error generando dieta: ${e.message}")
                _generacionState.value = Resource.Error("Error al conectar con el motor IA: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun procesarYGuardarJSON(jsonString: String, codUsuario: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            // A. Guardar Plan Diario
            val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val plan = PlanDiario(
                codUsuario = codUsuario,
                fechaInicio = fechaHoy,
                fechaFin = fechaHoy,
                objetivoPlan = jsonObject.getString("objetivoPlan"),
                porcentCumplimiento = 0.0,
                versionIA = "gemini-2.5-flash"
            )
            val codPlan = planRepository.guardarPlan(plan) ?: throw Exception("Fallo al guardar Plan")

            // B. Guardar Dieta Base
            val dieta = Dieta(
                kcalDieta = jsonObject.getDouble("kcalDieta"),
                nombreDieta = jsonObject.getString("nombreDieta")
            )

            val comidasArray = jsonObject.getJSONArray("comidas")
            val detallesAInsertar = mutableListOf<DetalleDieta>()

            // C. Procesar cada comida
            for (i in 0 until comidasArray.length()) {
                val comidaObj = comidasArray.getJSONObject(i)
                val tipoComida = comidaObj.getString("tipoComida")
                val alimentosArray = comidaObj.getJSONArray("alimentos")

                val detalle = DetalleDieta(
                    cantDetDieta = alimentosArray.length(),
                    tipoComida = tipoComida
                )
                detallesAInsertar.add(detalle)

                // D. Guardar los alimentos sugeridos
                for (j in 0 until alimentosArray.length()) {
                    val alimObj = alimentosArray.getJSONObject(j)
                    val alimento = Alimento(
                        nombreAlimento = alimObj.getString("nombreAlimento"),
                        kcalBase = alimObj.getDouble("kcalBase"),
                        proteinasBase = alimObj.getDouble("proteinasBase"),
                        carbohidratosBase = alimObj.getDouble("carbohidratosBase"),
                        grasasBase = alimObj.getDouble("grasasBase"),
                        indiceGlucemico = alimObj.getInt("indiceGlucemico")
                    )
                    alimentoRepository.guardarAlimento(alimento)
                }
            }

            dietaRepository.guardarDietaCompleta(dieta, detallesAInsertar)
            _generacionState.value = Resource.Success(true)

        } catch (e: Exception) {
            Log.e("JSON_PARSE_ERROR", "Error procesando JSON de IA: ${e.message}")
            _generacionState.value = Resource.Error("La respuesta de la IA no pudo ser procesada.")
        }
    }

    fun resetGeneracionState() { _generacionState.value = null }
}