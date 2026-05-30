package com.example.diadoc.repository

import com.example.diadoc.model.PerfilMedico
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PerfilMedicoRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarPerfilMedico(perfil: PerfilMedico): String? {
        return try {
            val document = if (perfil.codPerfil.isEmpty()) {
                db.collection("perfilesMedicos").document()
            } else {
                db.collection("perfilesMedicos").document(perfil.codPerfil)
            }

            val perfilParaGuardar = perfil.copy(codPerfil = document.id)
            document.set(perfilParaGuardar).await()
            document.id // Retornamos el ID generado para usarlo en las relaciones
        } catch (e: Exception) {
            null
        }
    }

    // Guarda la relación de Patologías sin alterar la data class
    suspend fun guardarPatologiasDelPerfil(codPerfil: String, codigosPatologias: List<String>) {
        val batch = db.batch()
        codigosPatologias.forEach { codPatologia ->
            val docRef = db.collection("perfilesMedicos").document(codPerfil)
                .collection("patologias_asociadas").document(codPatologia)
            batch.set(docRef, mapOf("codPatologia" to codPatologia))
        }
        batch.commit().await()
    }

    // Guarda la relación de Restricciones sin alterar la data class
    suspend fun guardarRestriccionesDelPerfil(codPerfil: String, codigosRestricciones: List<String>) {
        val batch = db.batch()
        codigosRestricciones.forEach { codRestriccion ->
            val docRef = db.collection("perfilesMedicos").document(codPerfil)
                .collection("restricciones_asociadas").document(codRestriccion)
            batch.set(docRef, mapOf("codRestricc" to codRestriccion))
        }
        batch.commit().await()
    }

    suspend fun obtenerPerfilPorUsuario(codUsuario: String): PerfilMedico? {
        return try {
            val snapshot = db.collection("perfilesMedicos")
                .whereEqualTo("codUsuario", codUsuario)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(PerfilMedico::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}