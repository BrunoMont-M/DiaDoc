package com.example.diadoc.model

data class PlanDiario(
    val codPlan: String = "",
    val codUsuario: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val objetivoPlan: String = "",
    val porcentCumplimiento: Double = 0.0,
    val versionIA: String = ""
)