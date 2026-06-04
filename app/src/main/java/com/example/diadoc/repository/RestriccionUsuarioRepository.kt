package com.example.diadoc.repository

import com.example.diadoc.model.RestriccionUsuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RestriccionUsuarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarRestriccion(restriccion: RestriccionUsuario): String? {
        return try {
            val document = if (restriccion.codRestricc.isEmpty()) {
                db.collection("restricciones").document()
            } else {
                db.collection("restricciones").document(restriccion.codRestricc)
            }

            val restriccionGuardar = restriccion.copy(codRestricc = document.id)
            document.set(restriccionGuardar).await()
            document.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerTodasLasRestricciones(): List<RestriccionUsuario> {
        return try {
            val snapshot = db.collection("restricciones").get().await()
            snapshot.toObjects(RestriccionUsuario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}