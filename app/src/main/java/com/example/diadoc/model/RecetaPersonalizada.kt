package com.example.diadoc.model

data class RecetaPersonalizada(
    val idReceta: String = "",
    val idUsuario: String = "",          // Agregado para saber qué paciente la creó
    val nombreReceta: String = "",
    val instruccionesReceta: String = ""
)