package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.Alimento
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel // Asegurate de que este import sea el correcto en tu proyecto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoAlimentosScreen(
    viewModel: CatalogoAlimentosViewModel, // <-- ACÁ RECIBE EL VIEWMODEL COMO PIDE APPNAVIGATION
    onBackClick: () -> Unit = {}
) {
    // Lista de prueba usando tu estructura de datos oficial del DC
    var listaAlimentos by remember {
        mutableStateOf(
            listOf(
                Alimento(
                    codAlimento = "1",
                    nombreAlimento = "Pechuga de pollo",
                    kcalBase = 165.0,
                    proteinasBase = 31.0,
                    carbohidratosBase = 0.0,
                    grasasBase = 3.6,
                    indiceGlucemico = 0
                ),
                Alimento(
                    codAlimento = "2",
                    nombreAlimento = "Arroz integral",
                    kcalBase = 111.0,
                    proteinasBase = 2.6,
                    carbohidratosBase = 23.0,
                    grasasBase = 0.9,
                    indiceGlucemico = 50
                ),
                Alimento(
                    codAlimento = "3",
                    nombreAlimento = "Manzana roja",
                    kcalBase = 52.0,
                    proteinasBase = 0.3,
                    carbohidratosBase = 14.0,
                    grasasBase = 0.2,
                    indiceGlucemico = 38
                )
            )
        )
    }

    // Estados para los Diálogos de ABM
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var alimentoAEditar by remember { mutableStateOf<Alimento?>(null) }

    // Campos del formulario
    var nombreInput by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var proteinasInput by remember { mutableStateOf("") }
    var carbohidratosInput by remember { mutableStateOf("") }
    var grasasInput by remember { mutableStateOf("") }
    var igInput by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryBlue = Color(0xFF00668B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Alimentos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nombreInput = ""
                    kcalInput = ""
                    proteinasInput = ""
                    carbohidratosInput = ""
                    grasasInput = ""
                    igInput = "0"
                    showAddDialog = true
                },
                containerColor = primaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Alimento")
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Gestión del Catálogo Maestro (Admin)",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listaAlimentos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay alimentos en el catálogo maestro", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaAlimentos) { alimento ->
                        AlimentoItem(
                            alimento = alimento,
                            cardColor = cardColor,
                            onEditClick = {
                                alimentoAEditar = alimento
                                nombreInput = alimento.nombreAlimento
                                kcalInput = alimento.kcalBase.toString()
                                proteinasInput = alimento.proteinasBase.toString()
                                carbohidratosInput = alimento.carbohidratosBase.toString()
                                grasasInput = alimento.grasasBase.toString()
                                igInput = alimento.indiceGlucemico.toString()
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGO AGREGAR ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuevo Alimento Base") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(value = kcalInput, onValueChange = { kcalInput = it }, label = { Text("Kcal por 100g") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = proteinasInput, onValueChange = { proteinasInput = it }, label = { Text("Proteínas (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = carbohidratosInput, onValueChange = { carbohidratosInput = it }, label = { Text("Carbohidratos (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = grasasInput, onValueChange = { grasasInput = it }, label = { Text("Grasas (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = igInput, onValueChange = { igInput = it }, label = { Text("Índice Glucémico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nombreInput.isNotBlank()) {
                        val nuevo = Alimento(
                            codAlimento = System.currentTimeMillis().toString(),
                            nombreAlimento = nombreInput,
                            kcalBase = kcalInput.toDoubleOrNull() ?: 0.0,
                            proteinasBase = proteinasInput.toDoubleOrNull() ?: 0.0,
                            carbohidratosBase = carbohidratosInput.toDoubleOrNull() ?: 0.0,
                            grasasBase = grasasInput.toDoubleOrNull() ?: 0.0,
                            indiceGlucemico = igInput.toIntOrNull() ?: 0
                        )
                        listaAlimentos = listaAlimentos + nuevo
                    }
                    showAddDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }

    // --- DIÁLOGO EDITAR / ELIMINAR ---
    if (showEditDialog && alimentoAEditar != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(value = kcalInput, onValueChange = { kcalInput = it }, label = { Text("Kcal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = proteinasInput, onValueChange = { proteinasInput = it }, label = { Text("Proteínas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = carbohidratosInput, onValueChange = { carbohidratosInput = it }, label = { Text("Carbohidratos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = grasasInput, onValueChange = { grasasInput = it }, label = { Text("Grasas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextField(value = igInput, onValueChange = { igInput = it }, label = { Text("Índice Glucémico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            listaAlimentos = listaAlimentos.filter { it.codAlimento != alimentoAEditar?.codAlimento }
                            showEditDialog = false
                            alimentoAEditar = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) { Text("Eliminar") }

                    Button(onClick = {
                        listaAlimentos = listaAlimentos.map {
                            if (it.codAlimento == alimentoAEditar?.codAlimento) {
                                it.copy(
                                    nombreAlimento = nombreInput,
                                    kcalBase = kcalInput.toDoubleOrNull() ?: 0.0,
                                    proteinasBase = proteinasInput.toDoubleOrNull() ?: 0.0,
                                    carbohidratosBase = carbohidratosInput.toDoubleOrNull() ?: 0.0,
                                    grasasBase = grasasInput.toDoubleOrNull() ?: 0.0,
                                    indiceGlucemico = igInput.toIntOrNull() ?: 0
                                )
                            } else it
                        }
                        showEditDialog = false
                        alimentoAEditar = null
                    }) { Text("Guardar") }
                }
            }
        )
    }
}

@Composable
fun AlimentoItem(alimento: Alimento, cardColor: Color, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alimento.nombreAlimento, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kcal: ${alimento.kcalBase} | IG: ${alimento.indiceGlucemico}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "P: ${alimento.proteinasBase}g | C: ${alimento.carbohidratosBase}g | G: ${alimento.grasasBase}g",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onEditClick) {
                Text(text = "Editar", color = Color(0xFF00A3E0))
            }
        }
    }
}