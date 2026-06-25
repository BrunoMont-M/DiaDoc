package com.example.diadoc.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.Alimento
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel
import com.example.diadoc.viewmodel.RecetarioViewModel

// --- Modelos de datos visuales (Respetando el diseño de Carlos) ---
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
    uid: String, // <-- Inyectado
    recetarioViewModel: RecetarioViewModel, // <-- Inyectado
    catalogoViewModel: CatalogoAlimentosViewModel, // <-- Inyectado (Para búsqueda)
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Estados de los campos
    var nombreReceta by remember { mutableStateOf("") }
    var porciones by remember { mutableStateOf("") }

    // Lista mutable
    val ingredientes = remember { mutableStateListOf<IngredienteReceta>() }

    // Estados para el Buscador de Alimentos Inteligente (Lo que pediste replicar)
    var showFoodSelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var foodSearchQuery by remember { mutableStateOf("") }
    val foodResults by catalogoViewModel.alimentos.collectAsState(initial = emptyList())
    val focusManager = LocalFocusManager.current

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary
    val warningYellow = Color(0xFFFFB300)

    val cantPorciones = porciones.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val totalKcal = ingredientes.sumOf { it.kcal } / cantPorciones
    val totalProt = ingredientes.sumOf { it.prot } / cantPorciones
    val totalCarb = ingredientes.sumOf { it.carb } / cantPorciones
    val totalGras = ingredientes.sumOf { it.gras } / cantPorciones

    val puedeGuardar = ingredientes.size >= 2 && nombreReceta.isNotBlank()

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
                    text = if (puedeGuardar) "NUEVA RECETA (Lista para guardar)" else "NUEVA RECETA (Editando...)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                // --- BLOQUE 1: DATOS BÁSICOS ---
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
                            placeholder = { Text("Ej: Tortitas de avena integrales") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
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
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                // --- BLOQUE 2: INGREDIENTES ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("INGREDIENTES:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (ingredientes.isEmpty()) {
                            Text("Usa el botón inferior para buscar alimentos del catálogo.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            ingredientes.forEach { ingrediente ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("• ", color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("${ingrediente.nombre} (${ingrediente.cantidad})", color = Color.White, fontSize = 15.sp)
                                    }
                                    IconButton(onClick = { ingredientes.remove(ingrediente) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF4D4D), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { catalogoViewModel.cargarAlimentos(); foodSearchQuery = ""; showFoodSelector = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AGREGAR INGREDIENTE DEL CATÁLOGO", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // --- BLOQUE 3: VALORES  ---
                Text("VALORES POR PORCIÓN (Calculados)", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 ${totalKcal} Kcal", color = Color(0xFFFF7043), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("💪 ${totalProt}g Prot", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("📦 ${totalCarb}g Carb", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("🥑 ${totalGras}g Gras", color = Color(0xFF26A69A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- REGLA INFO (Respetando diseño inferior que borré antes) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = if (puedeGuardar) Color.Gray else warningYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Una receta debe contener al menos dos ingredientes.",
                        color = if (puedeGuardar) Color.Gray else warningYellow,
                        fontSize = 13.sp,
                        fontWeight = if (puedeGuardar) FontWeight.Normal else FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val textoInstrucciones = "Rinde: $porciones porciones.\n\nIngredientes agregados:\n" +
                                ingredientes.joinToString("\n") { "• ${it.nombre} (${it.cantidad})" } +
                                "\n\nPasos de preparación:\n(Sin especificar)"

                        recetarioViewModel.guardarRecetaManual(
                            codUsuario = uid,
                            nombre = nombreReceta,
                            instrucciones = textoInstrucciones,
                            kcal = totalKcal.toDouble(),
                            prot = totalProt.toDouble(),
                            carb = totalCarb.toDouble(),
                            onSuccess = {
                                Toast.makeText(context, "¡Receta '$nombreReceta' guardada en Firebase!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        )
                    },
                    enabled = puedeGuardar,
                    modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR RECETA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }

    if (showFoodSelector) {
        ModalBottomSheet(onDismissRequest = { showFoodSelector = false }, sheetState = sheetState, containerColor = cardColor) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Búsqueda de Ingrediente (100g)", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

                OutlinedTextField(
                    value = foodSearchQuery,
                    onValueChange = { foodSearchQuery = it; catalogoViewModel.buscarAlimentos(it) },
                    placeholder = { Text("Buscar en Catálogo maestro...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.DarkGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val filtrados = if (foodSearchQuery.isEmpty()) foodResults else foodResults.filter { it.nombreAlimento.contains(foodSearchQuery, ignoreCase = true) }

                    if (filtrados.isEmpty() && foodSearchQuery.isNotEmpty()) {
                        item { Text("No se encontraron resultados para '$foodSearchQuery' en Firebase.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                    }

                    items(filtrados) { alim: Alimento ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ingredientes.add(
                                        IngredienteReceta(
                                            id = alim.codAlimento,
                                            nombre = alim.nombreAlimento,
                                            cantidad = "100g (Pred)",
                                            kcal = alim.kcalBase.toInt(),
                                            prot = alim.proteinasBase.toInt(),
                                            carb = alim.carbohidratosBase.toInt(),
                                            gras = alim.grasasBase.toInt()
                                        )
                                    )
                                    focusManager.clearFocus()
                                    foodSearchQuery = ""
                                    showFoodSelector = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alim.nombreAlimento, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("${alim.kcalBase.toInt()} kcal | ${alim.proteinasBase.toInt()}g Prot / 100g", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}