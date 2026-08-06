package com.example.diadoc.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.viewmodel.CatalogoEjerciciosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoEjerciciosScreen(
    viewModel: CatalogoEjerciciosViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val listaEjercicios by viewModel.ejercicios.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Estados para los diálogos
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var ejercicioSeleccionado by remember { mutableStateOf<Ejercicio?>(null) }

    // Estados del formulario
    var nombreInput by remember { mutableStateOf("") }
    var impactoInput by remember { mutableStateOf("") }
    var grupoInput by remember { mutableStateOf("") }
    var descripcionInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }

    // Estados para los menús desplegables
    var impactoExpanded by remember { mutableStateOf(false) }
    var grupoExpanded by remember { mutableStateOf(false) }

    val opcionesImpacto = listOf("Bajo", "Medio", "Alto")
    val opcionesGrupo = listOf("Tren Superior", "Tren Inferior", "Espalda", "Core", "Cardio", "Cuerpo Completo")

    LaunchedEffect(Unit) {
        viewModel.cargarEjercicios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Ejercicios", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ejercicioSeleccionado = null
                    nombreInput = ""
                    impactoInput = opcionesImpacto[0]
                    grupoInput = opcionesGrupo[0]
                    descripcionInput = ""
                    urlInput = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Ejercicio")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // Barra de Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.buscarEjercicios(it)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Buscar ejercicio...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.buscarEjercicios("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (isLoading && listaEjercicios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (listaEjercicios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron ejercicios", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(listaEjercicios) { ejercicio ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ejercicio.nombreEjercicio, style = MaterialTheme.typography.titleLarge)
                                        if (ejercicio.descripcion.isNotBlank()) {
                                            Text(ejercicio.descripcion, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                        Text("Grupo: ${ejercicio.grupoMuscular} | Impacto: ${ejercicio.impactoMuscular}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        if (ejercicio.urlVideoTutorial.isNotBlank()) {
                                            Text(
                                                text = "Ver Video Tutorial ↗",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelLarge,
                                                textDecoration = TextDecoration.Underline,
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .clickable {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ejercicio.urlVideoTutorial)).apply {
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (_: Exception) {}
                                                    }
                                            )
                                        }
                                    }

                                    TextButton(onClick = {
                                        ejercicioSeleccionado = ejercicio
                                        nombreInput = ejercicio.nombreEjercicio
                                        impactoInput = if(ejercicio.impactoMuscular.isBlank()) opcionesImpacto[0] else ejercicio.impactoMuscular
                                        grupoInput = if(ejercicio.grupoMuscular.isBlank()) opcionesGrupo[0] else ejercicio.grupoMuscular
                                        descripcionInput = ejercicio.descripcion
                                        urlInput = ejercicio.urlVideoTutorial
                                        showDialog = true
                                    }) {
                                        Text("Editar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal unificado para Agregar/Modificar
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (ejercicioSeleccionado == null) "Nuevo Ejercicio" else "Modificar Ejercicio", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreInput,
                        onValueChange = { nombreInput = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = impactoExpanded,
                        onExpandedChange = { impactoExpanded = !impactoExpanded }
                    ) {
                        OutlinedTextField(
                            value = impactoInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Impacto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = impactoExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = impactoExpanded,
                            onDismissRequest = { impactoExpanded = false }
                        ) {
                            opcionesImpacto.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        impactoInput = opcion
                                        impactoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = grupoExpanded,
                        onExpandedChange = { grupoExpanded = !grupoExpanded }
                    ) {
                        OutlinedTextField(
                            value = grupoInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grupo Muscular") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = grupoExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = grupoExpanded,
                            onDismissRequest = { grupoExpanded = false }
                        ) {
                            opcionesGrupo.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        grupoInput = opcion
                                        grupoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = descripcionInput,
                        onValueChange = { descripcionInput = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL Video") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ejercicioSeleccionado != null) {
                        TextButton(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        if (nombreInput.isNotBlank()) {
                            val ejercicioAGuardar = Ejercicio(
                                codEjercicio = ejercicioSeleccionado?.codEjercicio ?: "",
                                nombreEjercicio = nombreInput,
                                impactoMuscular = impactoInput,
                                grupoMuscular = grupoInput,
                                descripcion = descripcionInput,
                                urlVideoTutorial = urlInput
                            )
                            viewModel.guardarOActualizarEjercicio(ejercicioAGuardar)
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
            text = { Text("¿Estás seguro de que deseas eliminar '${ejercicioSeleccionado?.nombreEjercicio}'? Esta acción eliminará la rutina de la base de datos de la IA.") },
            confirmButton = {
                Button(
                    onClick = {
                        ejercicioSeleccionado?.let { viewModel.eliminarEjercicio(it.codEjercicio) }
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