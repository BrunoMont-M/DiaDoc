package com.example.diadoc.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.Alimento
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel
import com.example.diadoc.viewmodel.RecetarioViewModel
import java.util.UUID

// --- Modelos de datos visuales (Se mantienen igual, es lógica) ---
data class IngredienteReceta(
    val id: String,
    val nombre: String,
    val cantidad: String,
    val kcal: Int,
    val prot: Int,
    val carb: Int,
    val gras: Int
)

data class PasoPreparacion(
    val id: String = UUID.randomUUID().toString(),
    val texto: String = "",
    val enEdicion: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecetaScreen(
    uid: String,
    recetarioViewModel: RecetarioViewModel,
    catalogoViewModel: CatalogoAlimentosViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Estados de los campos básicos
    var nombreReceta by remember { mutableStateOf("") }
    var porciones by remember { mutableStateOf("") }

    var tipoComidaSeleccionada by remember { mutableStateOf("Desayuno") }
    val tiposDeComida = listOf(
        "Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena"
    )

    // Lista mutable de ingredientes
    val ingredientes = remember { mutableStateListOf<IngredienteReceta>() }

    // Lista mutable interactiva para los pasos de preparación
    val pasosPreparacion = remember { mutableStateListOf(PasoPreparacion()) }
    val ordinales = listOf("primer", "segundo", "tercer", "cuarto", "quinto", "sexto", "séptimo", "octavo", "noveno", "décimo")

    // Estados para el "Buscador de Alimentos Inteligente"
    var showFoodSelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var foodSearchQuery by remember { mutableStateOf("") }
    val foodResults by catalogoViewModel.alimentos.collectAsState(initial = emptyList())
    val focusManager = LocalFocusManager.current

    // Cálculos
    val cantPorciones = porciones.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val totalKcal = if (ingredientes.isNotEmpty()) ingredientes.sumOf { it.kcal } / cantPorciones else 0
    val totalProt = if (ingredientes.isNotEmpty()) ingredientes.sumOf { it.prot } / cantPorciones else 0
    val totalCarb = if (ingredientes.isNotEmpty()) ingredientes.sumOf { it.carb } / cantPorciones else 0
    val totalGras = if (ingredientes.isNotEmpty()) ingredientes.sumOf { it.gras } / cantPorciones else 0

    val puedeGuardar = ingredientes.size >= 2 && nombreReceta.isNotBlank() && porciones.isNotBlank()

    // Colores semánticos del tema
    val dangerColor = DiaDocTheme.colors.alertDanger
    val warningColor = MaterialTheme.colorScheme.error // Usamos error para la advertencia de validación
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Recetas", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    color = onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                // --- BLOQUE 1: DATOS BÁSICOS ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Categoría:", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tiposDeComida.forEach { tipo ->
                                val isSelected = tipoComidaSeleccionada == tipo
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { tipoComidaSeleccionada = tipo }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = tipo,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = porciones,
                            onValueChange = { porciones = it },
                            label = { Text("Rinde (Porciones)") },
                            placeholder = { Text("Ej: 4") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }

                // --- BLOQUE 2: INGREDIENTES ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("INGREDIENTES:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (ingredientes.isEmpty()) {
                            Text("Usa el botón inferior para buscar alimentos del catálogo.", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            ingredientes.forEach { ingrediente ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text("• ", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${ingrediente.nombre} (${ingrediente.cantidad})", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    IconButton(onClick = { ingredientes.remove(ingrediente) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = dangerColor, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { catalogoViewModel.cargarAlimentos(); foodSearchQuery = ""; showFoodSelector = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AGREGAR INGREDIENTE", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // --- BLOQUE 3: PASOS DE PREPARACIÓN ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("PASOS DE PREPARACIÓN:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        pasosPreparacion.forEachIndexed { index, paso ->
                            val sugerenciaOrdinal = if (index < ordinales.size) {
                                "Agregue el ${ordinales[index]} paso"
                            } else {
                                "Agregue el paso ${index + 1}"
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 14.dp, end = 8.dp)
                                )

                                OutlinedTextField(
                                    value = paso.texto,
                                    onValueChange = { nuevoValor -> pasosPreparacion[index] = paso.copy(texto = nuevoValor) },
                                    placeholder = { Text(sugerenciaOrdinal) },
                                    readOnly = !paso.enEdicion,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (paso.enEdicion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        unfocusedTextColor = if (paso.enEdicion) MaterialTheme.colorScheme.onSurface else onSurfaceVariant
                                    )
                                )

                                Row(modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
                                    if (paso.enEdicion) {
                                        IconButton(
                                            onClick = { pasosPreparacion[index] = paso.copy(enEdicion = false) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Confirmar paso", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { pasosPreparacion[index] = paso.copy(enEdicion = true) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Modificar paso", tint = onSurfaceVariant)
                                        }
                                    }
                                    IconButton(
                                        onClick = { pasosPreparacion.removeAt(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar paso", tint = dangerColor)
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { pasosPreparacion.add(PasoPreparacion()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AGREGAR PASO", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // --- BLOQUE 4: VALORES CALCULADOS ---
                Text("VALORES POR PORCIÓN (Calculados)", color = onSurfaceVariant, style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mantenemos colores específicos de nutrición y quitamos las llaves redundantes {}
                        Text("🔥 $totalKcal Kcal", color = Color(0xFFFF7043), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("💪 $totalProt g Prot", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("📦 $totalCarb g Carb", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("🥑 $totalGras g Gras", color = Color(0xFF26A69A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- REGLA INFO (Validación) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = if (puedeGuardar) onSurfaceVariant else warningColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Una receta debe contener al menos dos ingredientes.",
                        color = if (puedeGuardar) onSurfaceVariant else warningColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (puedeGuardar) FontWeight.Normal else FontWeight.Medium
                    )
                }

                DiaDocButton(
                    text = "GUARDAR RECETA",
                    icon = rememberVectorPainter(Icons.Default.Save),
                    enabled = puedeGuardar,
                    onClick = {
                        // Lógica de guardado
                        val pasosValidos = pasosPreparacion.filter { it.texto.isNotBlank() }
                        val textoPasos = if (pasosValidos.isNotEmpty()) {
                            pasosValidos.mapIndexed { i, paso -> "${i + 1}. ${paso.texto}" }.joinToString("\n")
                        } else {
                            "(Sin especificar)"
                        }

                        val textoInstrucciones = "Rinde: $porciones porciones.\n\nIngredientes agregados:\n" +
                                ingredientes.joinToString("\n") { "• ${it.nombre} (${it.cantidad})" } +
                                "\n\nPasos de preparación:\n$textoPasos"

                        recetarioViewModel.guardarRecetaManual(
                            codUsuario = uid,
                            nombre = nombreReceta,
                            tipoComida = tipoComidaSeleccionada,
                            instrucciones = textoInstrucciones,
                            kcal = totalKcal.toDouble(),
                            prot = totalProt.toDouble(),
                            carb = totalCarb.toDouble(),
                            onSuccess = {
                                Toast.makeText(context, "¡Receta '$nombreReceta' guardada!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 16.dp)
                )
            }
        }
    }

    if (showFoodSelector) {
        ModalBottomSheet(
            onDismissRequest = { showFoodSelector = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface // Usamos el color del tema
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Búsqueda de Ingrediente (100g)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                OutlinedTextField(
                    value = foodSearchQuery,
                    onValueChange = { foodSearchQuery = it; catalogoViewModel.buscarAlimentos(it) },
                    placeholder = { Text("Buscar en Catálogo maestro...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val filtrados = if (foodSearchQuery.isEmpty()) foodResults else foodResults.filter { it.nombreAlimento.contains(foodSearchQuery, ignoreCase = true) }

                    if (filtrados.isEmpty() && foodSearchQuery.isNotEmpty()) {
                        item {
                            Text(
                                "No se encontraron resultados para '$foodSearchQuery'.",
                                color = onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
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
                            Icon(Icons.Default.AddCircle, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alim.nombreAlimento, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("${alim.kcalBase.toInt()} kcal | ${alim.proteinasBase.toInt()}g Prot / 100g", color = onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}