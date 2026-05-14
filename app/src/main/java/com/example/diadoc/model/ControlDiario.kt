package com.example.diadoc.model

data class ControlDiario(
    val idControl: String = "",
    val idUsuario: String = "",          // Agregado para vincularlo al paciente
    val fechaHoraControl: String = "",
    val momentoDiaControl: String = "",
    val notasPaciente: String = ""
)