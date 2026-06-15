package com.example.diadoc.model

data class Rutina(
    val codRutina: String = "",
    val intensidad: String = "",
    val nombreRutina: String = "",

    // Agregar al DC excepto la FK
    val codPlan: String = "",
    val completado: Boolean = false,
    val versionMotorIA: String = ""
)