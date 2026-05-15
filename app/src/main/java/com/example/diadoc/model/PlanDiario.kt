package com.example.diadoc.model

data class PlanDiario(
    val codPlan: String = "",
    val codUsuario: String = "",          // Agregado para vincularlo al paciente
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val objetivoPlan: String = "",
    val porcentCumplimiento: Double = 0.0,
    val versionIA: String = ""
)