package com.example.diadoc.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ConfiguracionRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun obtenerApiKeyGemini(): String {
        return try {
            val snapshot = db.collection("configuracion").document("api_keys").get().await()
            snapshot.getString("gemini_api_key") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}