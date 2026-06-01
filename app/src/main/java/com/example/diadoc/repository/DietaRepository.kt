package com.example.diadoc.repository

import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DietaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarDietaCompleta(dieta: Dieta, detalles: List<DetalleDieta>): String? {
        return try {
            val document = db.collection("dietas").document()
            val dietaGuardar = dieta.copy(codDieta = document.id)

            val batch = db.batch()

            batch.set(document, dietaGuardar)

            detalles.forEach { detalle ->
                val detalleDoc = document.collection("detalles_comidas").document()
                val detalleGuardar = detalle.copy(codDetDieta = detalleDoc.id)
                batch.set(detalleDoc, detalleGuardar)
            }

            batch.commit().await()
            document.id
        } catch (e: Exception) {
            null
        }
    }
}