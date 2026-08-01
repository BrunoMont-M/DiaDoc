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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
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

    LaunchedEffect(Unit) {
        viewModel.cargarEjercicios()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var ejercicioSeleccionado by remember { mutableStateOf<Ejercicio?>(null) }

    var nombreInput by remember { mutableStateOf("") }
    var impactoInput by remember { mutableStateOf("") }
    var grupoInput by remember { mutableStateOf("") }
    var descripcionInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }

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
                    nombreInput = ""
                    impactoInput = ""
                    grupoInput = ""
                    descripcionInput = ""
                    urlInput = ""
                    showAddDialog = true
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Gestión Maestro de Ejercicios (Admin)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (listaEjercicios.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay ejercicios en el catálogo maestro", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                            impactoInput = ejercicio.impactoMuscular
                                            grupoInput = ejercicio.grupoMuscular
                                            descripcionInput = ejercicio.descripcion
                                            urlInput = ejercicio.urlVideoTutorial
                                            showEditDialog = true
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
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuevo Ejercicio", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = impactoInput, onValueChange = { impactoInput = it }, label = { Text("Impacto (Bajo/Medio/Alto)") })
                    OutlinedTextField(value = grupoInput, onValueChange = { grupoInput = it }, label = { Text("Grupo Muscular") })
                    OutlinedTextField(value = descripcionInput, onValueChange = { descripcionInput = it }, label = { Text("Descripción") })
                    OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("URL Video") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nombreInput.isNotBlank()) {
                        val nuevoEjercicio = Ejercicio(
                            codEjercicio = "",
                            nombreEjercicio = nombreInput,
                            impactoMuscular = impactoInput,
                            grupoMuscular = grupoInput,
                            descripcion = descripcionInput,
                            urlVideoTutorial = urlInput
                        )
                        viewModel.guardarOActualizarEjercicio(nuevoEjercicio)
                    }
                    showAddDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showEditDialog && ejercicioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modificar Ejercicio", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = impactoInput, onValueChange = { impactoInput = it }, label = { Text("Impacto") })
                    OutlinedTextField(value = grupoInput, onValueChange = { grupoInput = it }, label = { Text("Grupo Muscular") })
                    OutlinedTextField(value = descripcionInput, onValueChange = { descripcionInput = it }, label = { Text("Descripción") })
                    OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("URL Video") })
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            ejercicioSeleccionado?.let { viewModel.eliminarEjercicio(it.codEjercicio) }
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = DiaDocTheme.colors.alertDanger) // Color Semántico!
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val ejercicioModificado = ejercicioSeleccionado?.copy(
                            nombreEjercicio = nombreInput,
                            impactoMuscular = impactoInput,
                            grupoMuscular = grupoInput,
                            descripcion = descripcionInput,
                            urlVideoTutorial = urlInput
                        )
                        if (ejercicioModificado != null) {
                            viewModel.guardarOActualizarEjercicio(ejercicioModificado)
                        }
                        showEditDialog = false
                    }) {
                        Text("Guardar")
                    }
                }
            }
        )
    }
}