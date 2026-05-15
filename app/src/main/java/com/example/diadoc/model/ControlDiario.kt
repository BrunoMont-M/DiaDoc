package com.example.diadoc.model

data class ControlDiario(
    val codControl: String = "",
    val codUsuario: String = "",          // Agregado para vincularlo al paciente
    val fechaHoraControl: String = "",
    val momentoDiaControl: String = "",
    val notasPaciente: String = ""
)