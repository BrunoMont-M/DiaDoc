package com.example.diadoc.model

data class RecetaPersonalizada(
    val codReceta: String = "",
    val codUsuario: String = "",          // Agregado para saber qué paciente la creó
    val nombreReceta: String = "",
    val instruccionesReceta: String = ""
)