package com.example.diadoc.model

data class DetalleDieta(
    val codDetDieta: String = "",
    val cantDetDieta: Int = 0,
    val tipoComida: String = "",

    // Nuevos campos para poder guardar la receta en sí
    val nombrePlato: String = "",
    val descripcionPlato: String = "",
    val preparacion: List<String> = emptyList(),
    val consumido: Boolean = false // Para guardar el estado del plato
)