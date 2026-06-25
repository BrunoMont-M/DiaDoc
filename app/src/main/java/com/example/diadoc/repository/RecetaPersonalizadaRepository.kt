package com.example.diadoc.repository

import com.example.diadoc.model.RecetaPersonalizada
import com.google.firebase.Timestamp
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

    // --- LOGICA DE LAZY DELETION ---
    suspend fun obtenerRecetas(codUsuario: String, tipoComida: String? = null): List<RecetaPersonalizada> {
        return try {
            var query: Query = db.collection("recetasPersonalizadas")
                .whereEqualTo("codUsuario", codUsuario)

            if (tipoComida != null && tipoComida != "Todas") {
                query = query.whereEqualTo("tipoComida", tipoComida)
            }

            val snapshot = query.get().await()
            val ahora = Timestamp.now()

            // Filtramos y limpiamos la base de datos en caliente
            val listaFiltrada = snapshot.documents.mapNotNull { doc ->
                val receta = doc.toObject(RecetaPersonalizada::class.java)?.copy(codReceta = doc.id)

                if (receta?.fechaExpiracion != null && receta.fechaExpiracion!!.seconds < ahora.seconds) {
                    // LAZY DELETION: Si la receta expiró, se dispara el borrado en Firestore en segundo plano
                    doc.reference.delete()
                    null // Retornamos null para excluirla inmediatamente de la vista del usuario
                } else {
                    receta // Si está vigente o es definitiva, se mantiene en la lista
                }
            }

            // Ordenamos el resultado limpio
            listaFiltrada.sortedWith(
                compareByDescending<RecetaPersonalizada> { it.esFavorita }
                    .thenBy { it.nombreReceta.lowercase() }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Alternar Favorito (Existente)
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

    // Eliminar Receta Manual
    suspend fun eliminarReceta(codReceta: String): Boolean {
        return try {
            db.collection("recetasPersonalizadas").document(codReceta).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}