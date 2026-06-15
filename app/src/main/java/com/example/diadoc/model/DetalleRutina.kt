package com.example.diadoc.model

data class DetalleRutina(
    val codDetalle: String = "",
    val ordenDetalle: Int = 0,
    val repeticionesDetalle: Int = 0,
    val seriesDetalle: Int = 0,
    val tiempoDescanso: Int = 0,

    // Agregar al DC excepto las FK
    val codRutina: String = "",
    val codEjercicio: String = "",
    val observacionesIA: String = "",
    val consumido: Boolean = false
)