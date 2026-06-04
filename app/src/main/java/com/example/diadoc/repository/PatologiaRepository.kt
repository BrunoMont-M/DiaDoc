package com.example.diadoc.repository

import com.example.diadoc.model.Patologia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PatologiaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarPatologia(patologia: Patologia): String? {
        return try {
            val document = if (patologia.codPatologia.isEmpty()) {
                db.collection("patologias").document()
            } else {
                db.collection("patologias").document(patologia.codPatologia)
            }

            val patologiaGuardar = patologia.copy(codPatologia = document.id)
            document.set(patologiaGuardar).await()
            document.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerTodasLasPatologias(): List<Patologia> {
        return try {
            val snapshot = db.collection("patologias").get().await()
            snapshot.toObjects(Patologia::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}