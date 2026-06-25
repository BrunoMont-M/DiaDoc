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
import com.example.diadoc.repository.RecetaPersonalizadaRepository
import com.example.diadoc.utils.Resource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeneradorPlanViewModel(
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository(),
    private val alimentoRepository: AlimentoRepository = AlimentoRepository(),
    private val recetaRepository: RecetaPersonalizadaRepository = RecetaPersonalizadaRepository()
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

                val recetasPrevias = recetaRepository.obtenerRecetas(codUsuario).map { it.nombreReceta }
                val historialNombres = if (recetasPrevias.isNotEmpty()) recetasPrevias.joinToString(", ") else "Ninguna"

                // PROMPT CORREGIDO: Estandarización de mayúsculas e ingredientes básicos
                val prompt = """
                    Actúa como un Nutricionista Clínico de Alta Especialidad y Chef Profesional. Tu objetivo es diseñar un plan de alimentación diario ESTRICTAMENTE PERSONALIZADO y SEGURO para un paciente con el siguiente perfil:
                    - Peso actual: ${perfil.pesoActual} kg
                    - Altura: ${perfil.alturaPerfil} cm
                    - Patologías diagnosticadas: $patologias
                    - Restricciones/Alergias alimentarias: $restricciones
                    
                    DIRECTRICES MÉDICAS CRÍTICAS:
                    1. Es VITAL para la salud del paciente que el menú respete rigurosamente las patologías y restricciones indicadas.
                    2. Aplica lógica clínica según su diagnóstico para elegir los ingredientes correctos.
                    3. Calcula un objetivo calórico coherente.
                    4. ESTRUCTURA DE COMIDAS (INQUEBRANTABLE): EXACTAMENTE 6 objetos en este orden escrito exactamente así: "Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda" y "Cena". NO omitas ni fusiones ingestas.
                    5. REGLA ANTI-DUPLICADOS: El paciente ya conoce estas recetas: $historialNombres. TIENES ESTRICTAMENTE PROHIBIDO generar recetas con estos nombres.

                    Tu respuesta DEBE ser ÚNICAMENTE un objeto JSON válido.
                    {
                      "objetivoPlan": "Mantenimiento / Déficit / Superávit",
                      "kcalDieta": 2000,
                      "nombreDieta": "Menú Clínico Personalizado",
                      "comidas": [
                        {
                          "tipoComida": "Desayuno",
                          "nombrePlato": "Nombre Gourmet",
                          "descripcionPlato": "Breve descripción clínica.",
                          "preparacion": [
                            "Paso 1 detallado."
                          ],
                          "alimentos": [
                            { "nombreAlimento": "Nombre del ingrediente BÁSICO Y SIMPLE (ej. Manzana, Pollo, Arroz. NUNCA nombres compuestos ni preparaciones)", "kcalBase": 100.0, "proteinasBase": 5.0, "carbohidratosBase": 10.0, "grasasBase": 2.0, "indiceGlucemico": 30 }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val rawText = response.text ?: ""

                // EXTRACCIÓN ROBUSTA DE JSON: Ignora basura antes o después del JSON
                val startIndex = rawText.indexOf("{")
                val endIndex = rawText.lastIndexOf("}")

                val jsonText = if (startIndex != -1 && endIndex != -1) {
                    rawText.substring(startIndex, endIndex + 1)
                } else {
                    throw Exception("La IA no devolvió una estructura válida.")
                }

                procesarYGuardarJSON(jsonText, codUsuario)

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error generando dieta: ${e.message}")
                _generacionState.value = Resource.Error("El motor IA está saturado. Por favor, intenta de nuevo.")
            }
        }
    }

    private suspend fun procesarYGuardarJSON(jsonString: String, codUsuario: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val planAnterior = planRepository.obtenerPlanDeHoy(codUsuario, fechaHoy)
            if (planAnterior != null) {
                val db = FirebaseFirestore.getInstance()
                val dietaAnterior = dietaRepository.obtenerDietaPorPlan(planAnterior.codPlan)
                if (dietaAnterior != null) {
                    db.collection("dietas").document(dietaAnterior.codDieta).delete().await()
                }
                db.collection("planesDiarios").document(planAnterior.codPlan).delete().await()
            }

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
            val catalogoGlobal = alimentoRepository.obtenerTodosLosAlimentos()

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
                    descripcionPlato = comidaObj.optString("descripcionPlato", "Preparación sugerida por IA."),
                    preparacion = preparacionList
                )

                val alimentosArray = comidaObj.getJSONArray("alimentos")
                val listaAlimentos = mutableListOf<Alimento>()

                for (j in 0 until alimentosArray.length()) {
                    val alimObj = alimentosArray.getJSONObject(j)
                    val nombreBuscado = alimObj.getString("nombreAlimento")

                    val alimentoExistente = catalogoGlobal.find { it.nombreAlimento.equals(nombreBuscado, ignoreCase = true) }

                    if (alimentoExistente != null) {
                        listaAlimentos.add(alimentoExistente)
                    } else {
                        val nuevoAlimento = Alimento(
                            nombreAlimento = nombreBuscado,
                            kcalBase = alimObj.getDouble("kcalBase"),
                            proteinasBase = alimObj.getDouble("proteinasBase"),
                            carbohidratosBase = alimObj.getDouble("carbohidratosBase"),
                            grasasBase = alimObj.getDouble("grasasBase"),
                            indiceGlucemico = alimObj.getInt("indiceGlucemico")
                        )
                        alimentoRepository.guardarAlimento(nuevoAlimento)
                        listaAlimentos.add(nuevoAlimento)
                    }
                }
                menuCompleto[detalle] = listaAlimentos
            }

            dietaRepository.guardarDietaCompleta(dieta, menuCompleto)
            _generacionState.value = Resource.Success(true)

        } catch (e: Exception) {
            Log.e("JSON_PARSE_ERROR", "Error: ${e.message}")
            _generacionState.value = Resource.Error("Error de validación al generar el menú.")
        }
    }

    fun resetGeneracionState() { _generacionState.value = null }
}