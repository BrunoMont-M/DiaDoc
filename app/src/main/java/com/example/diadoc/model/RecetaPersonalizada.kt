package com.example.diadoc.model

import com.google.firebase.Timestamp

data class RecetaPersonalizada(
    val codReceta: String = "",
    val codUsuario: String = "",
    val nombreReceta: String = "",
    val instruccionesReceta: String = "",
    val tipoComida: String = "General",     // Permite categorización (Desayuno, Almuerzo, etc.)
    val esFavorita: Boolean = false,        // Sistema de anclaje rápido
    val origenIA: Boolean = false,          // Trazabilidad del origen de los datos
    val guardadaDefinitiva: Boolean = true, // Inmunidad contra la política TTL
    val fechaExpiracion: Timestamp? = null, // Trigger para la eliminación automática
    val kcalTotales: Double = 0.0,
    val carbohidratosTotales: Double = 0.0,
    val proteinasTotales: Double = 0.0      // Métrica crítica para el desarrollo muscular
)