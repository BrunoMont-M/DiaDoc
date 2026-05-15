package com.example.diadoc.model

data class PerfilMedico(
    val codPerfil: String = "",
    val codUsuario: String = "",        // La "Clave Foránea" que lo une al paciente
    val pesoActual: Double = 0.0,
    val alturaPerfil: Double = 0.0,
    val grupoSanguineo: String = "",
    val alergias: String = ""
)