package com.example.diadoc.repository

import com.example.diadoc.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Esta función guarda el objeto Usuario completo en la colección "usuarios" de Firestore
    suspend fun guardarUsuario(usuario: Usuario): Boolean {
        return try {
            db.collection("usuarios")
                .document(usuario.codUsuario) // Usamos el ID que nos dio Auth
                .set(usuario)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Función para traer los datos de un usuario por su ID
    suspend fun obtenerUsuario(idUsuario: String): Usuario? {
        return try {
            val snapshot = db.collection("usuarios").document(idUsuario).get().await()
            snapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }
}