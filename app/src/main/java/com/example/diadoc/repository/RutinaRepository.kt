package com.example.diadoc.repository

import com.example.diadoc.model.DetalleRutina
import com.example.diadoc.model.Rutina
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RutinaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarRutinaCompleta(rutina: Rutina, detalles: List<DetalleRutina>): String? {
        return try {
            val document = db.collection("rutinas").document()
            val rutinaGuardar = rutina.copy(codRutina = document.id)

            val batch = db.batch()
            batch.set(document, rutinaGuardar)

            detalles.forEach { detalle ->
                val detalleDoc = document.collection("detalles_rutina").document()
                val detalleGuardar = detalle.copy(codDetalle = detalleDoc.id, codRutina = document.id)
                batch.set(detalleDoc, detalleGuardar)
            }

            batch.commit().await()
            document.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerRutinaPorPlan(codPlan: String): Rutina? {
        return try {
            val snap = db.collection("rutinas")
                .whereEqualTo("codPlan", codPlan)
                .get().await()

            if (!snap.isEmpty) snap.documents[0].toObject(Rutina::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerDetallesDeRutina(codRutina: String): List<DetalleRutina> {
        return try {
            val snap = db.collection("rutinas").document(codRutina)
                .collection("detalles_rutina")
                .orderBy("ordenDetalle")
                .get().await()

            snap.toObjects(DetalleRutina::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun marcarEjercicioComoConsumido(codRutina: String, codDetalle: String, consumido: Boolean): Boolean {
        return try {
            db.collection("rutinas").document(codRutina)
                .collection("detalles_rutina").document(codDetalle)
                .update("consumido", consumido)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarRutinasDelPlan(codPlan: String): Boolean {
        return try {
            val rutinasSnap = db.collection("rutinas")
                .whereEqualTo("codPlan", codPlan)
                .get().await()

            if (rutinasSnap.isEmpty) return true

            val batch = db.batch()

            for (rutinaDoc in rutinasSnap.documents) {
                val detallesSnap = rutinaDoc.reference.collection("detalles_rutina").get().await()
                for (detalleDoc in detallesSnap.documents) {
                    batch.delete(detalleDoc.reference)
                }
                batch.delete(rutinaDoc.reference)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarCargaEjercicio(
        codRutina: String,
        codDetalle: String,
        nuevasSeries: Int,
        nuevasRepeticiones: Int,
        nuevoDescanso: Int
    ): Boolean {
        return try {
            db.collection("rutinas").document(codRutina)
                .collection("detalles_rutina").document(codDetalle)
                .update(
                    mapOf(
                        "seriesDetalle" to nuevasSeries,
                        "repeticionesDetalle" to nuevasRepeticiones,
                        "tiempoDescanso" to nuevoDescanso
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarEjercicio(codRutina: String, codDetalle: String): Boolean {
        return try {
            db.collection("rutinas").document(codRutina)
                .collection("detalles_rutina").document(codDetalle)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun agregarEjercicioNuevo(codRutina: String, detalle: DetalleRutina): Boolean {
        return try {
            val detalleDoc = db.collection("rutinas").document(codRutina)
                .collection("detalles_rutina").document()

            val detalleGuardar = detalle.copy(
                codDetalle = detalleDoc.id,
                codRutina = codRutina
            )

            detalleDoc.set(detalleGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}