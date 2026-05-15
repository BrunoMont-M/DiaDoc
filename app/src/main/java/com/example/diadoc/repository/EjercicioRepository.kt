package com.example.diadoc.repository

import com.example.diadoc.model.Ejercicio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EjercicioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarEjercicio(ejercicio: Ejercicio): Boolean {
        return try {
            val document = if (ejercicio.codEjercicio.isEmpty()) {
                db.collection("ejercicios").document()
            } else {
                db.collection("ejercicios").document(ejercicio.codEjercicio)
            }

            val ejercicioGuardar = ejercicio.copy(codEjercicio = document.id)
            document.set(ejercicioGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerTodosLosEjercicios(): List<Ejercicio> {
        return try {
            val snapshot = db.collection("ejercicios").get().await()
            snapshot.toObjects(Ejercicio::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}