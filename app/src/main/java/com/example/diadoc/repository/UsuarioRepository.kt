package com.example.diadoc.repository

import com.example.diadoc.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarUsuario(usuario: Usuario): Boolean {
        return try {
            db.collection("usuarios")
                .document(usuario.codUsuario)
                .set(usuario)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerUsuario(idUsuario: String): Usuario? {
        return try {
            val snapshot = db.collection("usuarios").document(idUsuario).get().await()
            snapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarFechaNacimiento(codUsuario: String, fecha: String): Boolean {
        return try {
            db.collection("usuarios").document(codUsuario).update("fechaNacimiento", fecha).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}