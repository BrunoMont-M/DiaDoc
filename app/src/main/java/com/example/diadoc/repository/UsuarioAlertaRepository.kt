package com.example.diadoc.repository

import com.example.diadoc.model.UsuarioAlerta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioAlertaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarAlerta(alerta: UsuarioAlerta): Boolean {
        return try {
            val document = if (alerta.codAlerta.isEmpty()) {
                db.collection("alertas").document()
            } else {
                db.collection("alertas").document(alerta.codAlerta)
            }

            val alertaGuardar = alerta.copy(codAlerta = document.id)
            document.set(alertaGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerTodasLasAlertas(): List<UsuarioAlerta> {
        return try {
            val snapshot = db.collection("alertas").get().await()
            snapshot.toObjects(UsuarioAlerta::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}