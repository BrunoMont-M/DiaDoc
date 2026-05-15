package com.example.diadoc.repository

import com.example.diadoc.model.RecetaPersonalizada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RecetaPersonalizadaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarReceta(receta: RecetaPersonalizada): Boolean {
        return try {
            val document = if (receta.codReceta.isEmpty()) {
                db.collection("recetasPersonalizadas").document()
            } else {
                db.collection("recetasPersonalizadas").document(receta.codReceta)
            }

            val recetaGuardar = receta.copy(codReceta = document.id)
            document.set(recetaGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerRecetasPorUsuario(idUsuario: String): List<RecetaPersonalizada> {
        return try {
            val snapshot = db.collection("recetasPersonalizadas")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            snapshot.toObjects(RecetaPersonalizada::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}