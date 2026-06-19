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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryBlue = Color(0xFF00A3E0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Ejercicios", color = Color.White, fontWeight = FontWeight.Bold) },
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
                    impactoInput = ""
                    grupoInput = ""
                    descripcionInput = ""
                    urlInput = ""
                    showAddDialog = true
                },
                containerColor = primaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Ejercicio")
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryBlue
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Gestión Maestro de Ejercicios (Admin)", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

                    if (listaEjercicios.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay ejercicios en el catálogo maestro", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            items(listaEjercicios) { ejercicio ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ejercicio.nombreEjercicio, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            if (ejercicio.descripcion.isNotBlank()) {
                                                Text(ejercicio.descripcion, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                                            }
                                            Text("Grupo: ${ejercicio.grupoMuscular} | Impacto: ${ejercicio.impactoMuscular}", color = Color.Gray, fontSize = 12.sp)

                                            if (ejercicio.urlVideoTutorial.isNotBlank()) {
                                                Text(
                                                    text = "Ver Video Tutorial ↗",
                                                    color = primaryBlue,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
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
                                            Text("Editar", color = primaryBlue, fontWeight = FontWeight.Bold)
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
            title = { Text("Nuevo Ejercicio Base") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(value = impactoInput, onValueChange = { impactoInput = it }, label = { Text("Impacto (Bajo/Medio/Alto)") })
                    TextField(value = grupoInput, onValueChange = { grupoInput = it }, label = { Text("Grupo Muscular") })
                    TextField(value = descripcionInput, onValueChange = { descripcionInput = it }, label = { Text("Descripción") })
                    TextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("URL Video Tutorial") })
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
            title = { Text("Editar / Modificar Ejercicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(value = impactoInput, onValueChange = { impactoInput = it }, label = { Text("Impacto") })
                    TextField(value = grupoInput, onValueChange = { grupoInput = it }, label = { Text("Grupo Muscular") })
                    TextField(value = descripcionInput, onValueChange = { descripcionInput = it }, label = { Text("Descripción") })
                    TextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("URL Video Tutorial") })
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
                            ejercicioSeleccionado?.let {
                                viewModel.eliminarEjercicio(it.codEjercicio)
                            }
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
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