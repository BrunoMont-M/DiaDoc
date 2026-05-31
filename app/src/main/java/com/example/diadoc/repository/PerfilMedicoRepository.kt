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
            document.id
        } catch (e: Exception) {
            null
        }
    }

    // Guarda la relación de patologias limpiando las anteriores primero
    suspend fun guardarPatologiasDelPerfil(codPerfil: String, codigosPatologias: List<String>) {
        val subColeccion = db.collection("perfilesMedicos").document(codPerfil).collection("patologias_asociadas")
        val batch = db.batch()

        val snapshotPrevio = subColeccion.get().await()
        for (doc in snapshotPrevio.documents) {
            batch.delete(doc.reference)
        }

        codigosPatologias.forEach { codPatologia ->
            val docRef = subColeccion.document(codPatologia)
            batch.set(docRef, mapOf("codPatologia" to codPatologia))
        }

        batch.commit().await()
    }

    // Guarda la relación de restricciones limpiando las anteriores primero
    suspend fun guardarRestriccionesDelPerfil(codPerfil: String, codigosRestricciones: List<String>) {
        val subColeccion = db.collection("perfilesMedicos").document(codPerfil).collection("restricciones_asociadas")
        val batch = db.batch()

        val snapshotPrevio = subColeccion.get().await()
        for (doc in snapshotPrevio.documents) {
            batch.delete(doc.reference)
        }

        codigosRestricciones.forEach { codRestriccion ->
            val docRef = subColeccion.document(codRestriccion)
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

    suspend fun obtenerPatologiasDelPerfil(codPerfil: String): List<String> {
        return try {
            val snapshot = db.collection("perfilesMedicos").document(codPerfil)
                .collection("patologias_asociadas")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("codPatologia") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerRestriccionesDelPerfil(codPerfil: String): List<String> {
        return try {
            val snapshot = db.collection("perfilesMedicos").document(codPerfil)
                .collection("restricciones_asociadas")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("codRestricc") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}