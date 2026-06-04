package com.example.diadoc.model

data class Alimento(
    val codAlimento: String = "",
    val nombreAlimento: String = "",
    val codQRAlimento: String = "",
    val kcalBase: Double = 0.0,
    val proteinasBase: Double = 0.0,
    val carbohidratosBase: Double = 0.0,
    val grasasBase: Double = 0.0,
    val indiceGlucemico: Int = 0,
    val alergenos: List<String> = emptyList() // Para validar restricciones médicas
)