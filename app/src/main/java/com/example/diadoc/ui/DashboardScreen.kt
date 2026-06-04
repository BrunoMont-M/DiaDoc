package com.example.diadoc.ui

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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    uid: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToGenerador: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val patologias by viewModel.patologias.collectAsState()
    val vasosAgua by viewModel.vasosAgua.collectAsState()
    val planHoy by viewModel.planHoy.collectAsState()
    val metricaDinamica by viewModel.metricaDinamica.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val comidasHoy by viewModel.comidasHoy.collectAsState()

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) { viewModel.refrescarPantalla(uid) }
    }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) refreshState.endRefresh()
    }

    LaunchedEffect(uid) { viewModel.cargarUsuario(uid) }

    var agendaExpanded by remember { mutableStateOf(true) }
    var infoPopupType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¡Hola, ${usuario?.nomYapeUsuario?.split(" ")?.get(0) ?: "Cargando"}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tu resumen de salud de hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                containerColor = Color(0xFFE53935),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "S.O.S")
            }
        }
        // Ya no declaramos bottomBar aquí, porque AppNavigation la dibuja globalmente
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(refreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { if (planHoy != null) viewModel.sumarVasoAgua() },
                        label = { Text(if (planHoy != null) "+1 Vaso de Agua" else "Generá un plan") },
                        leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF29B6F6)) },
                        shape = RoundedCornerShape(16.dp)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Modal Métrica */ },
                        label = { Text("Registrar Métrica") },
                        leadingIcon = { Icon(Icons.Default.Addchart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(16.dp)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Check-in Actividad */ },
                        label = { Text("Entrené Hoy") },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF66BB6A)) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val iconoTarjeta = when {
                        patologias.contains("diabet") -> Icons.Default.Bloodtype
                        patologias.contains("sarcopenia") -> Icons.Default.MonitorWeight
                        else -> Icons.Default.LocalFireDepartment
                    }
                    val colorTarjeta = when {
                        patologias.contains("diabet") -> Color(0xFFE53935)
                        patologias.contains("sarcopenia") -> Color(0xFF8E24AA)
                        else -> Color(0xFFFF9800)
                    }

                    if (metricaDinamica.size >= 4) {
                        TarjetaClinica(
                            titulo = metricaDinamica[0], valor = metricaDinamica[1],
                            unidad = metricaDinamica[2], subtexto = metricaDinamica[3],
                            icono = iconoTarjeta, colorPrimario = colorTarjeta
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Progreso Diario", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AnilloProgreso(
                                progreso = planHoy?.porcentCumplimiento?.toFloat() ?: 0f,
                                color = Color(0xFF66BB6A), icono = Icons.Default.Restaurant,
                                texto = "${((planHoy?.porcentCumplimiento ?: 0.0) * 100).toInt()}% Dieta",
                                onClick = { infoPopupType = "DIETA" }
                            )
                            AnilloProgreso(
                                progreso = if (planHoy != null) 0.8f else 0f,
                                color = Color(0xFFFFA726), icono = Icons.Default.DirectionsRun,
                                texto = "Ejercicio", onClick = { infoPopupType = "EJERCICIO" }
                            )
                            AnilloProgreso(
                                progreso = (vasosAgua / 8f).coerceIn(0f, 1f),
                                color = Color(0xFF29B6F6), icono = Icons.Default.LocalDrink,
                                texto = "$vasosAgua/8 Vasos", onClick = { infoPopupType = "AGUA" }
                            )
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
                                Text("Agenda Interna de DiaDoc", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Icon(if (agendaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                        }

                        if (agendaExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Comida Principal", fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = if (planHoy != null) "Plan activo: Sigue tu menú sugerido." else "Sugerido por IA (Plan pendiente)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateToGenerador, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (planHoy != null) "Regenerar Plan con IA" else "Generar Plan con IA")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }

            if (refreshState.progress > 0f || refreshState.isRefreshing) {
                PullToRefreshContainer(state = refreshState, modifier = Modifier.align(Alignment.TopCenter), containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
            }
        }

        // POP-UPS DETALLADOS
        if (infoPopupType != null) {
            AlertDialog(
                onDismissRequest = { infoPopupType = null },
                title = { Text("Detalle del Objetivo", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        when (infoPopupType) {
                            "DIETA" -> {
                                Text("Progreso de comidas sugeridas de hoy:", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (comidasHoy.isEmpty()) {
                                    Text("Aún no hay menú generado.", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                                } else {
                                    comidasHoy.forEach { comida ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                            Icon(if (comida.consumido) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (comida.consumido) Color(0xFF4CAF50) else Color.Gray, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(comida.tipoComida, fontSize = 14.sp, color = if (comida.consumido) MaterialTheme.colorScheme.onSurface else Color.Gray)
                                        }
                                    }
                                }
                            }
                            "AGUA" -> {
                                Text("La hidratación mejora la sensibilidad a la insulina y la digestión.", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                for (i in 1..8) {
                                    val check = i <= vasosAgua
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Icon(if (check) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (check) Color(0xFF29B6F6) else Color.Gray, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Vaso $i", fontSize = 14.sp, color = if (check) MaterialTheme.colorScheme.onSurface else Color.Gray)
                                    }
                                }
                            }
                            "EJERCICIO" -> {
                                Text("Módulo de rutinas en construcción.", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rutina Diaria (Pendiente)", fontSize = 14.sp, color = Color.Gray)
                                }
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
fun TarjetaClinica(titulo: String, valor: String, unidad: String, subtexto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, colorPrimario: Color) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(containerColor = colorPrimario.copy(alpha = 0.1f)), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(colorPrimario.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(icono, contentDescription = null, tint = colorPrimario) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(titulo, fontWeight = FontWeight.Bold, color = colorPrimario)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(unidad, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtexto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnilloProgreso(progreso: Float, color: Color, icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(8.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
            CircularProgressIndicator(progress = 1f, color = color.copy(alpha = 0.2f), strokeWidth = 6.dp, modifier = Modifier.fillMaxSize())
            CircularProgressIndicator(progress = progreso, color = color, strokeWidth = 6.dp, strokeCap = StrokeCap.Round, modifier = Modifier.fillMaxSize())
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}