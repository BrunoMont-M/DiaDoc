package com.example.diadoc.model

data class PerfilMedico(
    val idPerfil: String = "",
    val idUsuario: String = "",        // La "Clave Foránea" que lo une al paciente
    val pesoActual: Double = 0.0,
    val altura: Double = 0.0,
    val grupoSanguineo: String = "",
    val patologias: String = "",       // Ej: "Diabetes Tipo 2"
    val alergias: String = ""
)