package com.example.diadoc.model

data class Ejercicio(
    val idEjercicio: String = "",
    val nombreEjercicio: String = "",
    val impactoMuscular: String = "", // Bajo, Medio, Alto
    val grupoMuscular: String = "",   // Piernas, Espalda, etc.
    val descripcion: String = ""
)