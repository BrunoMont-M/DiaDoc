package com.example.diadoc.repository

import com.google.firebase.auth.FirebaseAuth
import com.example.diadoc.utils.Resource
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    // Lógica para registrar un usuario en Firebase Auth
    suspend fun registrarUsuario(email: String, password: String): Resource<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error desconocido en el registro")
        }
    }

    // Lógica para iniciar sesión
    suspend fun iniciarSesion(email: String, password: String): Resource<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al iniciar sesión")
        }
    }
}