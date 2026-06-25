package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.AuthRepository
import com.example.diadoc.repository.UsuarioRepository
import com.example.diadoc.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<String>?>(null)
    val authState: StateFlow<Resource<String>?> = _authState

    private val _resetPasswordState = MutableStateFlow<Resource<String>?>(null)
    val resetPasswordState: StateFlow<Resource<String>?> = _resetPasswordState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.iniciarSesion(email, password)
        }
    }

    fun register(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val authResult = authRepository.registrarUsuario(email, password)

            if (authResult is Resource.Success) {
                val uid = authResult.data

                val fechaHoraActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

                val nuevoUsuario = Usuario(
                    codUsuario = uid,
                    emailUsuario = email,
                    nomYapeUsuario = nombre,
                    fechaRegistro = fechaHoraActual
                )
                val guardadoExitoso = usuarioRepository.guardarUsuario(nuevoUsuario)

                if (guardadoExitoso) {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val nuevoDocRef = db.collection("usuarioEstados").document()

                        val nuevoEstado = hashMapOf(
                            "codUsuarioEstado" to nuevoDocRef.id,
                            "codUsuario" to uid,
                            "codEstadoU" to "ESTADO_ACTIVO",
                            "fechaDesdeUEstado" to fechaHoraActual,
                            "fechaHastaUEstado" to ""
                        )

                        nuevoDocRef.set(nuevoEstado).await()

                        _authState.value = Resource.Success(uid)
                    } catch (e: Exception) {
                        // Si ocurre un error de red menor al crear el estado, el usuario ya fue creado en Auth y BD.
                        // Permitimos el éxito para no interrumpir el Onboarding.
                        _authState.value = Resource.Success(uid)
                    }
                } else {
                    _authState.value = Resource.Error("Acceso creado, pero error al guardar perfil.")
                }
            } else {
                _authState.value = authResult
            }
        }
    }

    fun recuperarPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = Resource.Error("Por favor, ingresá tu email para recuperar la contraseña.")
            return
        }
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading
            _resetPasswordState.value = authRepository.recuperarPassword(email)
        }
    }

    fun resetState() { _authState.value = null }
    fun clearResetPasswordState() { _resetPasswordState.value = null }
}