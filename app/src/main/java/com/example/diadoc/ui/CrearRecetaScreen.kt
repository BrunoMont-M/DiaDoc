package com.example.diadoc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Modelos de datos temporales para la vista ---
data class IngredienteReceta(
    val id: String,
    val nombre: String,
    val cantidad: String,
    val kcal: Int,
    val prot: Int,
    val carb: Int,
    val gras: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecetaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCatalogoAlimentos: () -> Unit
) {
    // Estados de los campos basados en el boceto
    var nombreReceta by remember { mutableStateOf("Tortitas de avena integrales") }
    var porciones by remember { mutableStateOf("4") }

    // Lista mutable simulando los ingredientes agregados
    val ingredientes = remember {
        mutableStateListOf(
            IngredienteReceta("1", "Avena Instantánea", "100g", 360, 13, 60, 7),
            IngredienteReceta("2", "Huevo", "2 unidades", 140, 12, 1, 10),
            IngredienteReceta("3", "Leche Descremada", "200ml", 70, 6, 10, 0)
        )
    }

    // Paleta de colores Diadoc (Oscuro)
    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary
    val warningYellow = Color(0xFFFFB300)

    // --- LÓGICA DE CÁLCULO DE VALORES POR PORCIÓN ---
    val cantPorciones = porciones.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val totalKcal = ingredientes.sumOf { it.kcal } / cantPorciones
    val totalProt = ingredientes.sumOf { it.prot } / cantPorciones
    val totalCarb = ingredientes.sumOf { it.carb } / cantPorciones
    val totalGras = ingredientes.sumOf { it.gras } / cantPorciones

    // Validación de regla de negocio: al menos 2 ingredientes
    val puedeGuardar = ingredientes.size >= 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Recetas y Alimentos", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TÍTULO DE ACCIÓN
                Text(
                    text = "NUEVA RECETA (Editando...)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                // --- BLOQUE 1: DATOS BÁSICOS DE LA RECETA ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = nombreReceta,
                            onValueChange = { nombreReceta = it },
                            label = { Text("Nombre de la receta") },
                            placeholder = { Text("Ej: Tortitas de avena") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = porciones,
                            onValueChange = { porciones = it },
                            label = { Text("Rinde (Porciones)") },
                            placeholder = { Text("Ej: 4") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(0.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                // --- BLOQUE 2: LISTA DE INGREDIENTES ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "INGREDIENTES:",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (ingredientes.isEmpty()) {
                            Text(
                                text = "No hay ingredientes agregados aún.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            ingredientes.forEach { ingrediente ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "• ", color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${ingrediente.nombre} (${ingrediente.cantidad})",
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { ingredientes.remove(ingrediente) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFF4D4D))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar ingrediente",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // BOTÓN AGREGAR INGREDIENTE
                        OutlinedButton(
                            onClick = onNavigateToCatalogoAlimentos,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AGREGAR INGREDIENTE DEL CATÁLOGO", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // --- BLOQUE 3: VALORES POR PORCIÓN CALCULADOS ---
                Text(
                    text = "VALORES POR PORCIÓN (Calculados)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥 ${totalKcal} Kcal", color = Color(0xFFFF7043), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "💪 ${totalProt}g Prot", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "📦 ${totalCarb}g Carb", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "🥑 ${totalGras}g Gras", color = Color(0xFF26A69A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- REGLA DE NEGOCIO E INFO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (puedeGuardar) Color.Gray else warningYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Una receta debe contener al menos dos ingredientes.",
                        color = if (puedeGuardar) Color.Gray else warningYellow,
                        fontSize = 13.sp,
                        fontWeight = if (puedeGuardar) FontWeight.Normal else FontWeight.Medium
                    )
                }

                // --- BOTÓN ACCIÓN: GUARDAR RECETA ---
                Button(
                    onClick = { /* Próximo paso: persistencia */ },
                    enabled = puedeGuardar,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR RECETA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}