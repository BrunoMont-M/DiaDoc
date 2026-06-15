package com.example.diadoc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.DetalleRutina
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.repository.EjercicioRepository
import com.example.diadoc.viewmodel.GeneradorRutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRutinaScreen(
    viewModel: GeneradorRutinaViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val detallesRutina by viewModel.detallesRutina.collectAsState()
    val rutinaActual by viewModel.rutinaActual.collectAsState()
    val errorEdicion by viewModel.errorEdicion.collectAsState()

    val catalogoEjercicios = remember { mutableStateListOf<Ejercicio>() }
    var showCatalogoModal by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<DetalleRutina?>(null) }

    LaunchedEffect(uid) {
        viewModel.cargarRutinaDeHoy(uid)
        catalogoEjercicios.clear()
        catalogoEjercicios.addAll(EjercicioRepository().obtenerTodosLosEjercicios())
    }

    if (errorEdicion != null) {
        AlertDialog(
            onDismissRequest = { viewModel.limpiarErrorEdicion() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alerta Médica", fontWeight = FontWeight.Bold, color = Color.Red)
                }
            },
            text = { Text(errorEdicion!!) },
            confirmButton = {
                Button(
                    onClick = { viewModel.limpiarErrorEdicion() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showEditDialog != null) {
        val detalle = showEditDialog!!
        var series by remember { mutableStateOf(detalle.seriesDetalle.toString()) }
        var reps by remember { mutableStateOf(detalle.repeticionesDetalle.toString()) }
        var descanso by remember { mutableStateOf(detalle.tiempoDescanso.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Ajustar Carga", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = series,
                        onValueChange = { series = it },
                        label = { Text("Series") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Repeticiones / Minutos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descanso,
                        onValueChange = { descanso = it },
                        label = { Text("Descanso (segundos)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    rutinaActual?.codRutina?.let { codRutina ->
                        viewModel.actualizarCargaEjercicio(
                            codRutina,
                            detalle.codDetalle,
                            series.toIntOrNull() ?: detalle.seriesDetalle,
                            reps.toIntOrNull() ?: detalle.repeticionesDetalle,
                            descanso.toIntOrNull() ?: detalle.tiempoDescanso
                        )
                    }
                    showEditDialog = null
                }) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Entrenamiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Listo", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCatalogoModal = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                text = { Text("Agregar Ejercicio") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("RUTINA DE HOY (Editando...)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(detallesRutina) { detalle ->
                val ejercicioReal = catalogoEjercicios.find { it.codEjercicio == detalle.codEjercicio }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ejercicioReal?.nombreEjercicio ?: "Cargando...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                rutinaActual?.codRutina?.let { viewModel.eliminarEjercicio(it, detalle.codDetalle) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Impacto: ${ejercicioReal?.impactoMuscular ?: "..."}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Carga: ${detalle.seriesDetalle} x ${detalle.repeticionesDetalle} | Desc: ${detalle.tiempoDescanso}s", color = Color.DarkGray)
                            TextButton(onClick = { showEditDialog = detalle }) {
                                Text("Editar Carga")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showCatalogoModal) {
            ModalBottomSheet(onDismissRequest = { showCatalogoModal = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight(0.8f)) {
                    Text("Catálogo de Ejercicios", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(catalogoEjercicios) { ej ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    rutinaActual?.codRutina?.let { cod ->
                                        viewModel.agregarEjercicioManual(uid, cod, ej)
                                    }
                                    showCatalogoModal = false
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(ej.nombreEjercicio, fontWeight = FontWeight.Bold)
                                        Text("Impacto: ${ej.impactoMuscular}", fontSize = 14.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}