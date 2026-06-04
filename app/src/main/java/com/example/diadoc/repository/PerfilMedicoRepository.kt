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

            val nombresPatologias = mutableListOf<String>()

            for (doc in snapshot.documents) {
                val codPatologia = doc.getString("codPatologia")
                if (codPatologia != null) {
                    val patologiaDoc = db.collection("patologias").document(codPatologia).get().await()

                    val nombreEnfermedad = patologiaDoc.getString("nombreEnfermedad")

                    if (nombreEnfermedad != null) {
                        nombresPatologias.add(nombreEnfermedad)
                    } else {
                        nombresPatologias.add(codPatologia)
                    }
                }
            }
            nombresPatologias
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

            val nombresRestricciones = mutableListOf<String>()

            for (doc in snapshot.documents) {
                val codRestricc = doc.getString("codRestricc")
                if (codRestricc != null) {
                    val restriccionDoc = db.collection("restricciones").document(codRestricc).get().await()
                    val nombreRestriccion = restriccionDoc.getString("nombreRestriccion")

                    if (nombreRestriccion != null) {
                        nombresRestricciones.add(nombreRestriccion)
                    } else {
                        nombresRestricciones.add(codRestricc)
                    }
                }
            }
            nombresRestricciones
        } catch (e: Exception) {
            emptyList()
        }
    }
}