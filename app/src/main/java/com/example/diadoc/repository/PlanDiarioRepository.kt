package com.example.diadoc.repository

import com.example.diadoc.model.PlanDiario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlanDiarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarPlan(plan: PlanDiario): Boolean {
        return try {
            val document = if (plan.codPlan.isEmpty()) {
                db.collection("planesDiarios").document()
            } else {
                db.collection("planesDiarios").document(plan.codPlan)
            }

            val planGuardar = plan.copy(codPlan = document.id)
            document.set(planGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerPlanesPorUsuario(idUsuario: String): List<PlanDiario> {
        return try {
            val snapshot = db.collection("planesDiarios")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            snapshot.toObjects(PlanDiario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}