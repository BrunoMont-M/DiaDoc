package com.example.diadoc.repository

import com.example.diadoc.model.PerfilMedico
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PerfilMedicoRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarPerfilMedico(perfil: PerfilMedico): Boolean {
        return try {
            // Si el perfil no tiene ID, le decimos a Firebase que genere uno automático
            val document = if (perfil.codPerfil.isEmpty()) {
                db.collection("perfilesMedicos").document()
            } else {
                db.collection("perfilesMedicos").document(perfil.codPerfil)
            }

            val perfilParaGuardar = perfil.copy(codPerfil = document.id)
            document.set(perfilParaGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerPerfilPorUsuario(idUsuario: String): PerfilMedico? {
        return try {
            val snapshot = db.collection("perfilesMedicos")
                .whereEqualTo("idUsuario", idUsuario)
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