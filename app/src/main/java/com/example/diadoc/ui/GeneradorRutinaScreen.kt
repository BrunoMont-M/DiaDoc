package com.example.diadoc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.DetalleRutina
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.repository.EjercicioRepository
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.GeneradorRutinaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneradorRutinaScreen(
    viewModel: GeneradorRutinaViewModel,
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val rutinaActual by viewModel.rutinaActual.collectAsState()
    val detallesRutina by viewModel.detallesRutina.collectAsState()

    val catalogoEjercicios = remember { mutableStateListOf<Ejercicio>() }
    val refreshState = rememberPullToRefreshState()

    val moduleExercise = DiaDocTheme.colors.moduleExercise
    val alertDanger = DiaDocTheme.colors.alertDanger
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.cargarRutinaDeHoy(uid)
            catalogoEjercicios.clear()
            catalogoEjercicios.addAll(EjercicioRepository().obtenerTodosLosEjercicios())
            delay(500)
            refreshState.endRefresh()
        }
    }

    LaunchedEffect(uid) {
        viewModel.cargarRutinaDeHoy(uid)
        catalogoEjercicios.clear()
        catalogoEjercicios.addAll(EjercicioRepository().obtenerTodosLosEjercicios())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Entrenamiento Diario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
                .nestedScroll(refreshState.nestedScrollConnection)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Analizando tu perfil médico...\nGenerando rutina segura con IA",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("ENTRENAMIENTO DE HOY", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge, color = onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (rutinaActual != null) {
                                val completados = detallesRutina.count { it.consumido }
                                val total = detallesRutina.size
                                val progreso = if (total > 0) completados.toFloat() / total else 0f

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rutinaActual!!.nombreRutina,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${(progreso * 100).toInt()}% Completado",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = moduleExercise,
                                        textAlign = TextAlign.End
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progreso },
                                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                                    color = moduleExercise,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    item {
                        DiaDocButton(
                            text = if (rutinaActual == null) "GENERAR RUTINA CON IA" else "REGENERAR RUTINA",
                            icon = rememberVectorPainter(Icons.Default.AutoAwesome),
                            onClick = { viewModel.generarRutinaConIA(uid) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )

                        AnimatedVisibility(visible = error != null) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = alertDanger, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(error ?: "", color = alertDanger, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (rutinaActual == null && error == null) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(80.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Aún no tienes rutina para hoy.", color = onSurfaceVariant, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    } else {
                        items(detallesRutina) { detalle ->
                            val ejercicioReal = catalogoEjercicios.find { it.codEjercicio == detalle.codEjercicio }
                            EjercicioCard(
                                detalle = detalle,
                                ejercicio = ejercicioReal,
                                onCheckToggle = {
                                    rutinaActual?.codRutina?.let { codRutina ->
                                        viewModel.marcarEjercicioCompletado(codRutina, detalle.codDetalle, detalle.consumido)
                                    }
                                },
                                onNavigateToEditor = onNavigateToEditor
                            )
                        }

                        if (rutinaActual != null) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = rutinaActual!!.versionMotorIA,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }

            if (refreshState.progress > 0f || refreshState.isRefreshing) {
                PullToRefreshContainer(
                    state = refreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EjercicioCard(
    detalle: DetalleRutina,
    ejercicio: Ejercicio?,
    onCheckToggle: () -> Unit,
    onNavigateToEditor: () -> Unit
) {
    val moduleExercise = DiaDocTheme.colors.moduleExercise
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val estadoTexto = if (detalle.consumido) "Completado" else "Siguiente"
            Text("EJERCICIO ${detalle.ordenDetalle} ($estadoTexto)", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ejercicio?.nombreEjercicio ?: "Cargando ejercicio...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (detalle.tiempoDescanso > 0) {
                Text("Carga: ${detalle.seriesDetalle} Series x ${detalle.repeticionesDetalle} Repeticiones", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text("Descanso entre series: ${detalle.tiempoDescanso} seg", style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant)
            } else {
                Text("Tiempo: ${detalle.seriesDetalle} Minutos", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text("⚡", modifier = Modifier.padding(end = 4.dp))
                Column {
                    Text("Intensidad/Impacto: ${ejercicio?.impactoMuscular ?: "Desconocido"}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Ajustado por: ${detalle.observacionesIA}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckToggle() }) {
                    Checkbox(
                        checked = detalle.consumido,
                        onCheckedChange = { onCheckToggle() },
                        colors = CheckboxDefaults.colors(checkedColor = moduleExercise)
                    )
                    Text("Marcar como Hecho", fontWeight = if (detalle.consumido) FontWeight.Bold else FontWeight.Normal, color = if (detalle.consumido) moduleExercise else onSurfaceVariant)
                }

                TextButton(onClick = { onNavigateToEditor() }) {
                    Text("Cambiar ejercicio", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}