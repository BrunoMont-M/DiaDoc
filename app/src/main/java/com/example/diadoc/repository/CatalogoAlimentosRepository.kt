package com.example.diadoc.repository

import android.graphics.Bitmap
import com.example.diadoc.model.Alimento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class CatalogoAlimentosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val configRepository: ConfiguracionRepository = ConfiguracionRepository()
) {

    suspend fun buscarAlimentos(query: String): List<Alimento> {
        return try {
            val snapshot = db.collection("alimentos").get().await()
            val todosLosAlimentos = snapshot.toObjects(Alimento::class.java)

            if (query.isBlank()) {
                todosLosAlimentos
            } else {
                todosLosAlimentos.filter {
                    it.nombreAlimento.contains(query, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun guardarAlimento(alimento: Alimento) {
        val docRef = if (alimento.codAlimento.isEmpty()) {
            db.collection("alimentos").document() // Crea un nuevo ID
        } else {
            db.collection("alimentos").document(alimento.codAlimento) // Usa el ID existente para actualizar
        }

        val alimentoConId = alimento.copy(codAlimento = docRef.id)
        docRef.set(alimentoConId).await()
    }

    suspend fun eliminarAlimento(codAlimento: String) {
        if (codAlimento.isNotEmpty()) {
            db.collection("alimentos").document(codAlimento).delete().await()
        }
    }

    suspend fun analizarAlimentoConIA(bitmap: Bitmap): Alimento? {
        return try {
            val apiKey = configRepository.obtenerApiKeyGemini()
            if (apiKey.isEmpty()) {
                return null
            }

            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = apiKey
            )

            val inputContent = content {
                image(bitmap)
                text("Eres un nutricionista experto. Analiza la imagen de este alimento/plato. Devuelve un JSON estricto con los macronutrientes estimados por 100g. Formato exacto requerido: {\"nombre\": \"Nombre del alimento\", \"calorias\": 0.0, \"grasas\": 0.0, \"carbohidratos\": 0.0, \"proteinas\": 0.0}. No incluyas explicaciones, formato markdown ni texto extra, solo el objeto JSON.")
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: return null

            val json = JSONObject(responseText)

            Alimento(
                nombreAlimento = json.optString("nombre", "Alimento detectado"),
                kcalBase = json.optDouble("calorias", 0.0),
                grasasBase = json.optDouble("grasas", 0.0),
                carbohidratosBase = json.optDouble("carbohidratos", 0.0),
                proteinasBase = json.optDouble("proteinas", 0.0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}