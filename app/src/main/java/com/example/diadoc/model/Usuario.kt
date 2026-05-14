package com.example.diadoc.model

data class Usuario(
    val idUsuario: String = "",        // Firebase UID
    val nomYapeUsuario: String = "",
    val emailUsuario: String = "",
    val fechaNacimiento: String = "",  // Guardamos como String o Timestamp
    val fechaInicio: String = "",
    val telUsuario: String = "",
    val codRol: Int = 1                // 1 para Paciente, 2 para Admin
)
// Nota: La contraseña NO se guarda en este modelo. Firebase Auth la maneja internamente por seguridad.