package com.example.diadoc.repository

import com.example.diadoc.model.ControlDiario
import com.example.diadoc.model.DetalleControl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ControlDiarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun registrarControlCompleto(control: ControlDiario, detalle: DetalleControl): Boolean {
        return try {
            val batch = db.batch()

            val docControl = if (control.codControl.isEmpty()) {
                db.collection("controlesDiarios").document()
            } else {
                db.collection("controlesDiarios").document(control.codControl)
            }
            val controlGuardar = control.copy(codControl = docControl.id)
            batch.set(docControl, controlGuardar)

            val docDetalle = if (detalle.codDetControl.isEmpty()) {
                docControl.collection("detalles").document()
            } else {
                docControl.collection("detalles").document(detalle.codDetControl)
            }
            val detalleGuardar = detalle.copy(codDetControl = docDetalle.id)
            batch.set(docDetalle, detalleGuardar)

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerControlesPorUsuario(codUsuario: String): List<ControlDiario> {
        return try {
            val snapshot = db.collection("controlesDiarios")
                .whereEqualTo("codUsuario", codUsuario)
                .get()
                .await()

            snapshot.toObjects(ControlDiario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerDetallesDeControl(codControl: String): List<DetalleControl> {
        return try {
            val snapshot = db.collection("controlesDiarios").document(codControl)
                .collection("detalles")
                .get()
                .await()

            snapshot.toObjects(DetalleControl::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}