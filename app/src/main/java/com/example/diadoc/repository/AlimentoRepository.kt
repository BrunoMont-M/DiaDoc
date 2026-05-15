package com.example.diadoc.repository

import com.example.diadoc.model.Alimento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AlimentoRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarAlimento(alimento: Alimento): Boolean {
        return try {
            val document = if (alimento.codAlimento.isEmpty()) {
                db.collection("alimentos").document()
            } else {
                db.collection("alimentos").document(alimento.codAlimento)
            }

            val alimentoGuardar = alimento.copy(codAlimento = document.id)
            document.set(alimentoGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Trae todo el catálogo de alimentos
    suspend fun obtenerTodosLosAlimentos(): List<Alimento> {
        return try {
            val snapshot = db.collection("alimentos").get().await()
            snapshot.toObjects(Alimento::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Función clave para la US de escanear alimentos
    suspend fun obtenerAlimentoPorQR(codigoQR: String): Alimento? {
        return try {
            val snapshot = db.collection("alimentos")
                .whereEqualTo("codQRAlimento", codigoQR)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(Alimento::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}