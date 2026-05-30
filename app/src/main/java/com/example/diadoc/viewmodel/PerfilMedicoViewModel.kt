package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Patologia
import com.example.diadoc.model.PerfilMedico
import com.example.diadoc.model.RestriccionUsuario
import com.example.diadoc.model.Usuario
import com.example.diadoc.repository.PatologiaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.RestriccionUsuarioRepository
import com.example.diadoc.repository.UsuarioRepository
import com.example.diadoc.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilMedicoViewModel(
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val patologiaRepository: PatologiaRepository = PatologiaRepository(),
    private val restriccionRepository: RestriccionUsuarioRepository = RestriccionUsuarioRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _saveState = MutableStateFlow<Resource<Boolean>?>(null)
    val saveState: StateFlow<Resource<Boolean>?> = _saveState

    private val _patologias = MutableStateFlow<List<Patologia>>(emptyList())
    val patologias: StateFlow<List<Patologia>> = _patologias

    private val _restricciones = MutableStateFlow<List<RestriccionUsuario>>(emptyList())
    val restricciones: StateFlow<List<RestriccionUsuario>> = _restricciones

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun cargarDatosIniciales(codUsuario: String) {
        viewModelScope.launch {
            _usuario.value = usuarioRepository.obtenerUsuario(codUsuario)
            _patologias.value = patologiaRepository.obtenerTodasLasPatologias()
            _restricciones.value = restriccionRepository.obtenerTodasLasRestricciones()
        }
    }

    fun guardarPerfilCompleto(
        perfil: PerfilMedico,
        fechaNacimiento: String,
        patologiasSeleccionadas: List<String>,
        restriccionesSeleccionadas: List<String>
    ) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading

            // 1. Actualizamos la fecha de nacimiento en la colección de Usuario
            usuarioRepository.actualizarFechaNacimiento(perfil.codUsuario, fechaNacimiento)

            // 2. Guardamos el perfil principal
            val codPerfilGenerado = perfilRepository.guardarPerfilMedico(perfil)

            if (codPerfilGenerado != null) {
                // 3. Guardamos las relaciones
                perfilRepository.guardarPatologiasDelPerfil(codPerfilGenerado, patologiasSeleccionadas)
                perfilRepository.guardarRestriccionesDelPerfil(codPerfilGenerado, restriccionesSeleccionadas)
                _saveState.value = Resource.Success(true)
            } else {
                _saveState.value = Resource.Error("Error al guardar el perfil médico.")
            }
        }
    }

    suspend fun perfilExiste(codUsuario: String): Boolean {
        return perfilRepository.obtenerPerfilPorUsuario(codUsuario) != null
    }

    fun resetSaveState() { _saveState.value = null }
}