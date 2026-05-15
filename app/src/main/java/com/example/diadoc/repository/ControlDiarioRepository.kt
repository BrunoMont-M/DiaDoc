package com.example.diadoc.repository

import com.example.diadoc.model.ControlDiario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ControlDiarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun registrarControl(control: ControlDiario): Boolean {
        return try {
            val document = if (control.codControl.isEmpty()) {
                db.collection("controlesDiarios").document()
            } else {
                db.collection("controlesDiarios").document(control.codControl)
            }

            val controlGuardar = control.copy(codControl = document.id)
            document.set(controlGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Trae todos los controles de un paciente específico
    suspend fun obtenerControlesPorUsuario(idUsuario: String): List<ControlDiario> {
        return try {
            val snapshot = db.collection("controlesDiarios")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            snapshot.toObjects(ControlDiario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}