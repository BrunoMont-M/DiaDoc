package com.example.diadoc.model

data class UsuarioAlerta(
    val codAlerta: String = "",
    val estadoEnvio: String = "",
    val fechaHoraActivacion: String = "",
    val tipoAlerta: String = ""
)