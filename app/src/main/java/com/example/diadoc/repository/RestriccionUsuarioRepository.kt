package com.example.diadoc.repository

import com.example.diadoc.model.RestriccionUsuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RestriccionUsuarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun obtenerTodasLasRestricciones(): List<RestriccionUsuario> {
        return try {
            val snapshot = db.collection("restricciones").get().await()
            snapshot.toObjects(RestriccionUsuario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}