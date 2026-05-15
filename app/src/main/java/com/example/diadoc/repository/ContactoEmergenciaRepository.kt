package com.example.diadoc.repository

import com.example.diadoc.model.ContactoEmergencia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactoEmergenciaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarContacto(contacto: ContactoEmergencia): Boolean {
        return try {
            val document = if (contacto.codContacto.isEmpty()) {
                db.collection("contactosEmergencia").document()
            } else {
                db.collection("contactosEmergencia").document(contacto.codContacto)
            }

            val contactoGuardar = contacto.copy(codContacto = document.id)
            document.set(contactoGuardar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Trae únicamente los contactos del paciente que está logueado
    suspend fun obtenerContactosPorUsuario(idUsuario: String): List<ContactoEmergencia> {
        return try {
            val snapshot = db.collection("contactosEmergencia")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            snapshot.toObjects(ContactoEmergencia::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}