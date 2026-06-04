package com.example.diadoc.repository

import com.example.diadoc.model.PlanDiario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlanDiarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarPlan(plan: PlanDiario): String? {
        return try {
            val document = if (plan.codPlan.isEmpty()) {
                db.collection("planesDiarios").document()
            } else {
                db.collection("planesDiarios").document(plan.codPlan)
            }

            val planGuardar = plan.copy(codPlan = document.id)
            document.set(planGuardar).await()
            document.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerPlanesPorUsuario(codUsuario: String): List<PlanDiario> {
        return try {
            val snapshot = db.collection("planesDiarios")
                .whereEqualTo("codUsuario", codUsuario)
                .get()
                .await()
            snapshot.toObjects(PlanDiario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerPlanDeHoy(codUsuario: String, fechaHoy: String): PlanDiario? {
        return try {
            val snapshot = db.collection("planesDiarios")
                .whereEqualTo("codUsuario", codUsuario)
                .whereEqualTo("fechaInicio", fechaHoy)
                .get()
                .await()
            if (!snapshot.isEmpty) snapshot.documents[0].toObject(PlanDiario::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarVasosAgua(codPlan: String, nuevosVasos: Int): Boolean {
        return try {
            db.collection("planesDiarios").document(codPlan)
                .update("vasosAgua", nuevosVasos).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun actualizarProgresoDieta(codPlan: String, nuevoPorcentaje: Double): Boolean {
        return try {
            db.collection("planesDiarios").document(codPlan)
                .update("porcentCumplimiento", nuevoPorcentaje).await()
            true
        } catch (e: Exception) { false }
    }
}