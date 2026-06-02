package com.example.diadoc.repository

import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DietaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarDietaCompleta(dieta: Dieta, menuCompleto: Map<DetalleDieta, List<Alimento>>): String? {
        return try {
            val document = db.collection("dietas").document()
            val dietaGuardar = dieta.copy(codDieta = document.id)

            val batch = db.batch()
            batch.set(document, dietaGuardar)

            menuCompleto.forEach { (detalle, alimentos) ->
                val detalleDoc = document.collection("detalles_comidas").document()
                val detalleGuardar = detalle.copy(codDetDieta = detalleDoc.id)
                batch.set(detalleDoc, detalleGuardar)

                alimentos.forEach { alimento ->
                    val alimDoc = detalleDoc.collection("alimentos_detalle").document()
                    val alimGuardar = alimento.copy(codAlimento = alimDoc.id)
                    batch.set(alimDoc, alimGuardar)
                }
            }

            batch.commit().await()
            document.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerDietaPorPlan(codPlan: String): Dieta? {
        return try {
            val snap = db.collection("dietas")
                .whereEqualTo("codPlan", codPlan)
                .get().await()

            if (!snap.isEmpty) snap.documents[0].toObject(Dieta::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerMenuCompleto(codDieta: String): Map<DetalleDieta, List<Alimento>> {
        val menu = mutableMapOf<DetalleDieta, List<Alimento>>()
        try {
            val detallesSnap = db.collection("dietas").document(codDieta).collection("detalles_comidas").get().await()

            for (doc in detallesSnap.documents) {
                val detalle = doc.toObject(DetalleDieta::class.java)
                if (detalle != null) {
                    val alimentosSnap = doc.reference.collection("alimentos_detalle").get().await()
                    val alimentos = alimentosSnap.toObjects(Alimento::class.java)
                    menu[detalle] = alimentos
                }
            }
        } catch (e: Exception) { }
        return menu
    }
}