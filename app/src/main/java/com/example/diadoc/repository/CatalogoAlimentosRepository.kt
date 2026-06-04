package com.example.diadoc.repository

import com.example.diadoc.model.Alimento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CatalogoAlimentosRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

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
}