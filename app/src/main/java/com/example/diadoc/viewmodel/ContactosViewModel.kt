package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.ContactoEmergencia
import com.example.diadoc.repository.ContactoEmergenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactosViewModel(
    private val repository: ContactoEmergenciaRepository = ContactoEmergenciaRepository()
) : ViewModel() {

    private val _contactos = MutableStateFlow<List<ContactoEmergencia>>(emptyList())
    val contactos: StateFlow<List<ContactoEmergencia>> = _contactos

    private val _errorTelefono = MutableStateFlow<Boolean>(false)
    val errorTelefono: StateFlow<Boolean> = _errorTelefono

    fun cargarContactos(uid: String) {
        viewModelScope.launch {
            _contactos.value = repository.obtenerContactosPorUsuario(uid)
        }
    }

    fun guardarContacto(codUsuario: String, nombre: String, vinculo: String, telefono: String, codContactoEditar: String = "") {

        val isValidPhone = telefono.isNotBlank() && telefono.matches(Regex("^\\+?[0-9]+$"))

        if (!isValidPhone) {
            _errorTelefono.value = true
            return
        }

        _errorTelefono.value = false

        val contacto = ContactoEmergencia(
            codContacto = codContactoEditar,
            codUsuario = codUsuario,
            nombreContacto = nombre,
            vinculo = vinculo,
            telefono = telefono
        )

        viewModelScope.launch {
            val exito = repository.guardarContacto(contacto)
            if (exito) {
                cargarContactos(codUsuario)
            }
        }
    }

    fun eliminarContacto(codContacto: String, uid: String) {
        viewModelScope.launch {
            repository.eliminarContacto(codContacto)
            cargarContactos(uid)
        }
    }
}