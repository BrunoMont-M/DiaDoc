package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.AuthRepository
import com.example.diadoc.repository.UsuarioRepository
import com.example.diadoc.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
                val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                val nuevoUsuario = Usuario(
                    codUsuario = uid,
                    emailUsuario = email,
                    nomYapeUsuario = nombre,
                    fechaRegistro = fechaActual
                )
                val guardadoExitoso = usuarioRepository.guardarUsuario(nuevoUsuario)

                if (guardadoExitoso) {
                    _authState.value = Resource.Success(uid)
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

    fun resetState() {
        _authState.value = null
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = null
    }
}