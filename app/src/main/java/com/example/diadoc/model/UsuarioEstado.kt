package com.example.diadoc.model

data class UsuarioEstado(
    val codUsuarioEstado: String = "", // ID propio de este documento
    val codUsuario: String = "",       // FK: Apunta al UID del usuario
    val codEstadoU: String = "",       // FK: Apunta al Estado (Ej: "ESTADO_BAJA")
    val fechaDesdeUEstado: String = "",
    val fechaHastaUEstado: String = "" // Si está vacío, significa que es el estado actual
)