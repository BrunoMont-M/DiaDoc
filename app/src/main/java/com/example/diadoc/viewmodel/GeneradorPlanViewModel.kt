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

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun generarPlanParaUsuario(codUsuario: String) {
        viewModelScope.launch {
            _generacionState.value = Resource.Loading
            try {
                val perfil = perfilRepository.obtenerPerfilPorUsuario(codUsuario)
                if (perfil == null) {
                    _generacionState.value = Resource.Error("Perfil médico no encontrado.")
                    return@launch
                }

                val patologias = perfilRepository.obtenerPatologiasDelPerfil(perfil.codPerfil).joinToString(", ").ifBlank { "Ninguna reportada" }
                val restricciones = perfilRepository.obtenerRestriccionesDelPerfil(perfil.codPerfil).joinToString(", ").ifBlank { "Ninguna reportada" }

                // Prompt para la generación de recetas
                val prompt = """
                    Actúa como un Nutricionista Clínico de Alta Especialidad y Chef Profesional. Tu objetivo es diseñar un plan de alimentación diario ESTRICTAMENTE PERSONALIZADO y SEGURO para un paciente con el siguiente perfil:
                    - Peso actual: ${perfil.pesoActual} kg
                    - Altura: ${perfil.alturaPerfil} cm
                    - Patologías diagnosticadas: $patologias
                    - Restricciones/Alergias alimentarias: $restricciones
                    
                    DIRECTRICES MÉDICAS CRÍTICAS (DE CUMPLIMIENTO OBLIGATORIO):
                    1. Es VITAL para la salud del paciente que el menú respete rigurosamente las patologías y restricciones indicadas. Excluye por completo cualquier ingrediente que esté contraindicado para sus patologías o genere reacciones alérgicas según su perfil.
                    2. Aplica lógica clínica: Si tiene hipertensión, diseña platos bajos en sodio; si es celíaco, excluye totalmente el gluten; si es diabético, prioriza ingredientes de bajo índice glucémico; si no tiene restricciones, prioriza una dieta equilibrada y saludable.
                    3. Calcula un objetivo calórico coherente (mantenimiento, déficit o superávit moderado) basado en su peso y altura.

                    DIRECTRICES CULINARIAS Y DE EXPERIENCIA DE USUARIO:
                    Diseña un menú de autor, que sea atractivo, variado y delicioso. Evita nombres genéricos. Cada comida debe sentirse como una receta premium. Asigna un nombre atractivo al plato ("nombrePlato"), una descripción ("descripcionPlato") que explique de forma empática cómo este plato beneficia su condición clínica específica, y un arreglo de instrucciones paso a paso claras ("preparacion").
                    
                    Tu respuesta DEBE ser ÚNICAMENTE un objeto JSON válido con la siguiente estructura exacta, sin código Markdown ni texto adicional al principio o al final:
                    {
                      "objetivoPlan": "Mantenimiento / Déficit / Superávit",
                      "kcalDieta": 2000,
                      "nombreDieta": "Menú Clínico Personalizado",
                      "comidas": [
                        {
                          "tipoComida": "Desayuno",
                          "nombrePlato": "Nombre Gourmet y Atractivo",
                          "descripcionPlato": "Breve descripción destacando el impacto positivo del plato para sus patologías y nutrición.",
                          "preparacion": [
                            "Paso 1 detallado.",
                            "Paso 2 detallado."
                          ],
                          "alimentos": [
                            { "nombreAlimento": "Nombre del ingrediente", "kcalBase": 100.0, "proteinasBase": 5.0, "carbohidratosBase": 10.0, "grasasBase": 2.0, "indiceGlucemico": 30 }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonText = response.text?.replace("\u0060\u0060\u0060json", "")?.replace("\u0060\u0060\u0060", "")?.trim() ?: ""

                procesarYGuardarJSON(jsonText, codUsuario)

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error generando dieta: ${e.message}")
                _generacionState.value = Resource.Error("Error al conectar con el motor IA.")
            }
        }
    }

    private suspend fun procesarYGuardarJSON(jsonString: String, codUsuario: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val plan = PlanDiario(
                codUsuario = codUsuario,
                fechaInicio = fechaHoy,
                fechaFin = fechaHoy,
                objetivoPlan = jsonObject.getString("objetivoPlan"),
                porcentCumplimiento = 0.0,
                versionIA = "gemini-2.5-flash"
            )
            val codPlanGenerado = planRepository.guardarPlan(plan) ?: throw Exception("Fallo al guardar")

            val dieta = Dieta(
                codPlan = codPlanGenerado,
                kcalDieta = jsonObject.getDouble("kcalDieta"),
                nombreDieta = jsonObject.getString("nombreDieta")
            )

            val comidasArray = jsonObject.getJSONArray("comidas")
            val menuCompleto = mutableMapOf<DetalleDieta, List<Alimento>>()

            for (i in 0 until comidasArray.length()) {
                val comidaObj = comidasArray.getJSONObject(i)

                val preparacionArray = comidaObj.getJSONArray("preparacion")
                val preparacionList = mutableListOf<String>()
                for (k in 0 until preparacionArray.length()) {
                    preparacionList.add(preparacionArray.getString(k))
                }

                val detalle = DetalleDieta(
                    cantDetDieta = comidaObj.getJSONArray("alimentos").length(),
                    tipoComida = comidaObj.getString("tipoComida"),
                    nombrePlato = comidaObj.optString("nombrePlato", "Plato Saludable"),
                    descripcionPlato = comidaObj.optString("descripcionPlato", "Preparación nutritiva sugerida por IA."),
                    preparacion = preparacionList
                )

                val alimentosArray = comidaObj.getJSONArray("alimentos")
                val listaAlimentos = mutableListOf<Alimento>()

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
                    listaAlimentos.add(alimento)
                    alimentoRepository.guardarAlimento(alimento)
                }
                menuCompleto[detalle] = listaAlimentos
            }

            dietaRepository.guardarDietaCompleta(dieta, menuCompleto)
            _generacionState.value = Resource.Success(true)

        } catch (e: Exception) {
            Log.e("JSON_PARSE_ERROR", "Error: ${e.message}")
            _generacionState.value = Resource.Error("Error al procesar la receta.")
        }
    }

    fun resetGeneracionState() { _generacionState.value = null }
}