package com.example.diadoc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.Ejercicio
import com.example.diadoc.repository.EjercicioRepository
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.PdfManager
import com.example.diadoc.viewmodel.DashboardViewModel
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    uid: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToGenerador: () -> Unit,
    onNavigateToBitacora: () -> Unit,
    onNavigateToActividad: () -> Unit
) {
    val context = LocalContext.current
    val usuario by viewModel.usuario.collectAsState()
    val patologias by viewModel.patologias.collectAsState()
    val vasosAgua by viewModel.vasosAgua.collectAsState()
    val planHoy by viewModel.planHoy.collectAsState()
    val metricaDinamica by viewModel.metricaDinamica.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val comidasHoy by viewModel.comidasHoy.collectAsState()

    val ejerciciosHoy by viewModel.ejerciciosHoy.collectAsState()
    val porcentajeEjercicio by viewModel.porcentajeEjercicio.collectAsState()
    val catalogoEjercicios = remember { mutableStateListOf<Ejercicio>() }

    val rachaActual by viewModel.rachaActual.collectAsState()
    val tipDelDia by viewModel.tipDelDia.collectAsState()
    val alertaContextual by viewModel.alertaContextual.collectAsState()
    val historialMetricas by viewModel.historialMetricas.collectAsState()

    val comparativaSemanal by viewModel.comparativaSemanal.collectAsState()
    var showComparativaModal by remember { mutableStateOf(false) }

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refrescarPantalla(uid)
            viewModel.cargarProgresoEjercicio(uid)
        }
    }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) refreshState.endRefresh()
    }

    LaunchedEffect(uid) {
        viewModel.cargarUsuario(uid)
        viewModel.cargarProgresoEjercicio(uid)
        catalogoEjercicios.clear()
        catalogoEjercicios.addAll(EjercicioRepository().obtenerTodosLosEjercicios())
    }

    var agendaExpanded by remember { mutableStateOf(true) }
    var infoPopupType by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    var lastScrollOffset by remember { mutableStateOf(0) }
    var isFabExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.value) {
        isFabExpanded = scrollState.value <= lastScrollOffset || scrollState.value < 50
        lastScrollOffset = scrollState.value
    }

    // Variables de Tema Semántico
    val alertDanger = DiaDocTheme.colors.alertDanger
    val alertWarning = DiaDocTheme.colors.alertWarning
    val alertGood = DiaDocTheme.colors.alertGood
    val moduleNutrition = DiaDocTheme.colors.moduleNutrition
    val moduleExercise = DiaDocTheme.colors.moduleExercise
    val moduleHydration = DiaDocTheme.colors.moduleHydration
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¡Hola, ${usuario?.nomYapeUsuario?.split(" ")?.get(0) ?: "Cargando"}!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tu resumen de salud de hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                },
                actions = {
                    if (rachaActual > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(alertWarning.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Racha", tint = alertWarning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$rachaActual", fontWeight = FontWeight.Black, color = alertWarning, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    IconButton(onClick = {
                        PdfManager.generarYCompartirPDF(context, usuario, planHoy, metricaDinamica, rachaActual)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Exportar PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.primary)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = alertaContextual != null) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = alertDanger.copy(alpha = 0.1f)),
                        elevation = CardDefaults.elevatedCardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = "Alerta", tint = alertDanger)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(alertaContextual ?: "", color = alertDanger, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { if (planHoy != null) viewModel.sumarVasoAgua() },
                        label = { Text(if (planHoy != null) "+1 Vaso de Agua" else "Generá un plan") },
                        leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = moduleHydration) },
                        shape = RoundedCornerShape(16.dp)
                    )
                    FilterChip(
                        selected = false,
                        onClick = onNavigateToActividad,
                        label = { Text("Entrené Hoy") },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = moduleExercise) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                if (tipDelDia != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.mostrarTipAleatorio() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        elevation = CardDefaults.elevatedCardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Tip", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Tip Médico del Día", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(tipDelDia ?: "", style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val iconoTarjeta = when {
                        patologias.contains("diabet") -> Icons.Default.Bloodtype
                        patologias.contains("sarcopenia") -> Icons.Default.MonitorWeight
                        else -> Icons.Default.LocalFireDepartment
                    }

                    val valorActual = metricaDinamica.getOrNull(1)?.toFloatOrNull() ?: 0f
                    // Lógica de colores adaptada al nuevo Design System
                    val colorTarjeta = when {
                        patologias.contains("diabet") -> {
                            when {
                                valorActual == 0f || valorActual < 70f || valorActual > 140f -> alertDanger
                                valorActual <= 100f -> alertGood
                                else -> alertWarning
                            }
                        }
                        patologias.contains("sarcopenia") -> MaterialTheme.colorScheme.secondary // Color de la app para no alertar innecesariamente
                        else -> alertWarning // Default neutral/llamativo
                    }

                    if (metricaDinamica.size >= 4) {
                        TarjetaClinica(
                            titulo = metricaDinamica[0], valor = metricaDinamica[1],
                            unidad = metricaDinamica[2], subtexto = metricaDinamica[3],
                            icono = iconoTarjeta, colorPrimario = colorTarjeta,
                            historial = historialMetricas,
                            onClick = { showComparativaModal = true }
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Progreso Diario", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AnilloProgreso(
                                progreso = planHoy?.porcentCumplimiento?.toFloat() ?: 0f,
                                color = moduleNutrition, icono = Icons.Default.Restaurant,
                                texto = "${((planHoy?.porcentCumplimiento ?: 0.0) * 100).toInt()}% Dieta",
                                onClick = { infoPopupType = "DIETA" }
                            )
                            AnilloProgreso(
                                progreso = porcentajeEjercicio,
                                color = moduleExercise, icono = Icons.Default.DirectionsRun,
                                texto = "${(porcentajeEjercicio * 100).toInt()}% Ejercicio",
                                onClick = { infoPopupType = "EJERCICIO" }
                            )
                            AnilloProgreso(
                                progreso = (vasosAgua / 8f).coerceIn(0f, 1f),
                                color = moduleHydration, icono = Icons.Default.LocalDrink,
                                texto = "$vasosAgua/8 Vasos", onClick = { infoPopupType = "AGUA" }
                            )
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToBitacora() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bitácora de Salud", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("Registra métricas y check-in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a Bitácora", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                        .clickable { agendaExpanded = !agendaExpanded },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, contentDescription = "Agenda", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agenda Interna de DiaDoc", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Icon(if (agendaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                        }

                        if (agendaExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Comida Principal", style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                text = if (planHoy != null) "Plan activo: Sigue tu menú sugerido." else "Sugerido por IA (Plan pendiente)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp, end = 60.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            DiaDocButton(
                                text = if (planHoy != null) "Regenerar Plan con IA" else "Generar Plan con IA",
                                icon = rememberVectorPainter(Icons.Default.AutoAwesome),
                                onClick = onNavigateToGenerador,
                                modifier = Modifier.fillMaxWidth().padding(end = 40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            ExtendedFloatingActionButton(
                onClick = onNavigateToSOS,
                containerColor = alertDanger,
                contentColor = Color.White,
                shape = CircleShape,
                expanded = isFabExpanded,
                icon = { Icon(Icons.Default.Notifications, contentDescription = "S.O.S") },
                text = { Text("SOS", fontWeight = FontWeight.Black) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 92.dp)
            )

            if (refreshState.progress > 0f || refreshState.isRefreshing) {
                PullToRefreshContainer(state = refreshState, modifier = Modifier.align(Alignment.TopCenter), containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
            }
        }

        if (showComparativaModal) {
            ModalBottomSheet(
                onDismissRequest = { showComparativaModal = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Análisis Clínico Semanal", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(comparativaSemanal, style = MaterialTheme.typography.bodyLarge, color = textSecondary)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (infoPopupType != null) {
            AlertDialog(
                onDismissRequest = { infoPopupType = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Detalle del Objetivo", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        when (infoPopupType) {
                            "DIETA" -> {
                                Text("Progreso de comidas sugeridas de hoy:", style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (comidasHoy.isEmpty()) {
                                    Text("Aún no hay menú generado.", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                                } else {
                                    val ordenCronologico = listOf("Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena")
                                    val comidasOrdenadas = comidasHoy.sortedBy { comida ->
                                        val index = ordenCronologico.indexOfFirst { it.equals(comida.tipoComida, ignoreCase = true) }
                                        if (index != -1) index else 99
                                    }

                                    comidasOrdenadas.forEach { comida ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                            Icon(
                                                imageVector = if (comida.consumido) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (comida.consumido) moduleNutrition else textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(comida.tipoComida, style = MaterialTheme.typography.bodyMedium, color = if (comida.consumido) MaterialTheme.colorScheme.onSurface else textSecondary)
                                        }
                                    }
                                }
                            }
                            "AGUA" -> {
                                Text("La hidratación mejora la sensibilidad a la insulina y la digestión.", style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                                Spacer(modifier = Modifier.height(12.dp))
                                for (i in 1..8) {
                                    val check = i <= vasosAgua
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Icon(
                                            imageVector = if (check) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (check) moduleHydration else textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Vaso $i", style = MaterialTheme.typography.bodyMedium, color = if (check) MaterialTheme.colorScheme.onSurface else textSecondary)
                                    }
                                }
                            }
                            "EJERCICIO" -> {
                                Text("Rutina sugerida de hoy:", style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (ejerciciosHoy.isEmpty()) {
                                    Text("Aún no tienes rutina generada.", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                                } else {
                                    ejerciciosHoy.forEach { detalle ->
                                        val ej = catalogoEjercicios.find { it.codEjercicio == detalle.codEjercicio }
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                            Icon(
                                                imageVector = if (detalle.consumido) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (detalle.consumido) moduleExercise else textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(ej?.nombreEjercicio ?: "Cargando...", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                DiaDocButton(
                                    text = "IR A MI ENTRENAMIENTO",
                                    onClick = {
                                        infoPopupType = null
                                        onNavigateToActividad()
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { infoPopupType = null }) { Text("Cerrar") } },
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
        }
    }
}

@Composable
fun TarjetaClinica(
    titulo: String, valor: String, unidad: String, subtexto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector, colorPrimario: Color,
    historial: List<Float>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colorPrimario.copy(alpha = 0.1f)),
        elevation = CardDefaults.elevatedCardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(colorPrimario.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = colorPrimario)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorPrimario)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                // Para números muy grandes (métricas principales), usamos un estilo custom más grande pero derivado del theme
                Text(valor, fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(unidad, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtexto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (historial.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                val chartEntryModel = entryModelOf(*historial.toTypedArray())
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = colorPrimario, // El gráfico automáticamente tomará el color Danger, Warning o Good
                                lineBackgroundShader = null
                            )
                        )
                    ),
                    model = chartEntryModel,
                    modifier = Modifier.height(60.dp).fillMaxWidth()
                )
            } else if (historial.size == 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Registra un valor más para ver tu curva de tendencia.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun AnilloProgreso(progreso: Float, color: Color, icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
            CircularProgressIndicator(progress = { 1f }, color = color.copy(alpha = 0.2f), strokeWidth = 6.dp, modifier = Modifier.fillMaxSize())
            CircularProgressIndicator(progress = { progreso }, color = color, strokeWidth = 6.dp, strokeCap = StrokeCap.Round, modifier = Modifier.fillMaxSize())
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium)
    }
}