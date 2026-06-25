package com.example.diadoc.repository

import com.example.diadoc.model.RecetaPersonalizada
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun obtenerRecetas(codUsuario: String, tipoComida: String? = null): List<RecetaPersonalizada> {
        return try {
            var query: Query = db.collection("recetasPersonalizadas")
                .whereEqualTo("codUsuario", codUsuario)

            if (tipoComida != null && tipoComida != "Todas") {
                query = query.whereEqualTo("tipoComida", tipoComida)
            }

            val snapshot = query.get().await()

            val listaSinOrdenar = snapshot.toObjects(RecetaPersonalizada::class.java)

            listaSinOrdenar.sortedWith(
                compareByDescending<RecetaPersonalizada> { it.esFavorita }
                    .thenBy { it.nombreReceta.lowercase() }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun alternarFavorito(codReceta: String, estadoActual: Boolean): Boolean {
        return try {
            db.collection("recetasPersonalizadas")
                .document(codReceta)
                .update("esFavorita", !estadoActual)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarReceta(codReceta: String): Boolean {
        return try {
            db.collection("recetasPersonalizadas").document(codReceta).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}