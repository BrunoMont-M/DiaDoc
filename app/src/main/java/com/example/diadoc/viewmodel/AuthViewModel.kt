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

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<String>?>(null)
    val authState: StateFlow<Resource<String>?> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.iniciarSesion(email, password)
        }
    }

    fun register(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            // 1. Creamos el usuario en Firebase Auth
            val authResult = authRepository.registrarUsuario(email, password)

            if (authResult is Resource.Success) {
                // 2. Si Auth tuvo éxito, guardamos sus datos en Firestore
                val uid = authResult.data
                val nuevoUsuario = Usuario(
                    codUsuario = uid,
                    emailUsuario = email,
                    nomYapeUsuario = nombre
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

    fun resetState() { _authState.value = null }
}