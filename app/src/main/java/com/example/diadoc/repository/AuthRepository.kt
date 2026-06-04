package com.example.diadoc.repository

import com.example.diadoc.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun iniciarSesion(email: String, password: String): Resource<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(traducirErrorFirebase(e))
        }
    }

    suspend fun registrarUsuario(email: String, password: String): Resource<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(traducirErrorFirebase(e))
        }
    }

    suspend fun recuperarPassword(email: String): Resource<String> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success("Correo de recuperación enviado con éxito.")
        } catch (e: Exception) {
            Resource.Error(traducirErrorFirebase(e))
        }
    }

    private fun traducirErrorFirebase(e: Exception): String {
        val mensaje = e.message ?: return "Ocurrió un error desconocido."
        return when {
            mensaje.contains("incorrect, malformed or has expired") || mensaje.contains("invalid-credential") ->
                "El correo o la contraseña son incorrectos."
            mensaje.contains("email address is already in use") ->
                "Este correo ya está registrado. Iniciá sesión."
            mensaje.contains("badly formatted") ->
                "El formato del correo no es válido."
            mensaje.contains("network error") ->
                "Error de red. Verificá tu conexión a internet."
            mensaje.contains("password should be at least 6 characters") ->
                "La contraseña debe tener al menos 6 caracteres."
            else -> "Error de autenticación: Verifica tus datos."
        }
    }
}