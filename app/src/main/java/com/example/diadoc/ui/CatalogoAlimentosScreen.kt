package com.example.diadoc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.diadoc.model.Alimento
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoAlimentosScreen(
    viewModel: CatalogoAlimentosViewModel,
    onBackClick: () -> Unit = {}
) {
    val listaAlimentos by viewModel.alimentos.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Estados para los diálogos
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var alimentoSeleccionado by remember { mutableStateOf<Alimento?>(null) }

    // Estados del formulario
    var nombreInput by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var proteinasInput by remember { mutableStateOf("") }
    var carbohidratosInput by remember { mutableStateOf("") }
    var grasasInput by remember { mutableStateOf("") }
    var igInput by remember { mutableStateOf("") }
    var igExpanded by remember { mutableStateOf(false) }

    val opcionesIG = listOf("0 (Nulo/Agua)", "1-55 (Bajo)", "56-69 (Medio)", "70+ (Alto)")

    LaunchedEffect(Unit) {
        viewModel.cargarAlimentos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Alimentos", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    alimentoSeleccionado = null
                    nombreInput = ""
                    kcalInput = ""
                    proteinasInput = ""
                    carbohidratosInput = ""
                    grasasInput = ""
                    igInput = opcionesIG[0]
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Alimento")
            }
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
                    .padding(16.dp)
            ) {
                // Barra de Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.buscarAlimentos(it)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Buscar alimento...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.buscarAlimentos("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (isLoading && listaAlimentos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (listaAlimentos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No se encontraron alimentos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(listaAlimentos) { alimento ->
                            TarjetaAlimentoAdmin(
                                alimento = alimento,
                                onEditClick = {
                                    alimentoSeleccionado = alimento
                                    nombreInput = alimento.nombreAlimento
                                    kcalInput = alimento.kcalBase.toString()
                                    proteinasInput = alimento.proteinasBase.toString()
                                    carbohidratosInput = alimento.carbohidratosBase.toString()
                                    grasasInput = alimento.grasasBase.toString()
                                    igInput = if (alimento.indiceGlucemico == 0) opcionesIG[0]
                                    else if (alimento.indiceGlucemico <= 55) opcionesIG[1]
                                    else if (alimento.indiceGlucemico <= 69) opcionesIG[2]
                                    else opcionesIG[3]
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal para Agregar/Modificar
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (alimentoSeleccionado == null) "Nuevo Alimento" else "Modificar Alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreInput,
                        onValueChange = { nombreInput = it },
                        label = { Text("Nombre del alimento") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kcalInput,
                            onValueChange = { kcalInput = it },
                            label = { Text("Kcal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = proteinasInput,
                            onValueChange = { proteinasInput = it },
                            label = { Text("Proteínas (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = carbohidratosInput,
                            onValueChange = { carbohidratosInput = it },
                            label = { Text("Carbo (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = grasasInput,
                            onValueChange = { grasasInput = it },
                            label = { Text("Grasas (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = igExpanded,
                        onExpandedChange = { igExpanded = !igExpanded }
                    ) {
                        OutlinedTextField(
                            value = igInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Impacto Glucémico") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = igExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = igExpanded,
                            onDismissRequest = { igExpanded = false }
                        ) {
                            opcionesIG.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        igInput = opcion
                                        igExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (alimentoSeleccionado != null) {
                        TextButton(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val igNumber = when {
                            igInput.contains("0") -> 0
                            igInput.contains("55") -> 45
                            igInput.contains("69") -> 60
                            igInput.contains("70") -> 85
                            else -> 0
                        }

                        if (nombreInput.isNotBlank()) {
                            viewModel.guardarAlimento(
                                codAlimento = alimentoSeleccionado?.codAlimento,
                                nombre = nombreInput,
                                kcal = kcalInput.toDoubleOrNull() ?: 0.0,
                                grasas = grasasInput.toDoubleOrNull() ?: 0.0,
                                carbohidratos = carbohidratosInput.toDoubleOrNull() ?: 0.0,
                                proteinas = proteinasInput.toDoubleOrNull() ?: 0.0,
                                indiceGlucemico = igNumber,
                                alergenos = emptyList()
                            )
                        }
                        showDialog = false
                    }) {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Confirmación de Eliminación
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar '${alimentoSeleccionado?.nombreAlimento}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        alimentoSeleccionado?.let { viewModel.eliminarAlimento(it.codAlimento) }
                        showDeleteConfirmation = false
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TarjetaAlimentoAdmin(alimento: Alimento, onEditClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alimento.nombreAlimento, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kcal: ${alimento.kcalBase} | IG: ${alimento.indiceGlucemico}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "P: ${alimento.proteinasBase}g | C: ${alimento.carbohidratosBase}g | G: ${alimento.grasasBase}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onEditClick) {
                Text("Editar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}