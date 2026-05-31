package com.example.diadoc.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateToSOS: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()

    LaunchedEffect(uid) {
        viewModel.cargarUsuario(uid)
    }

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Inicio", "Nutrición", "Actividad", "Progreso")
    val icons = listOf(Icons.Default.Home, Icons.Default.Restaurant, Icons.Default.DirectionsRun, Icons.Default.TrendingUp)

    // Estados para expandir/contraer las tarjetas
    var glucosaExpanded by remember { mutableStateOf(false) }
    var cumplimientoExpanded by remember { mutableStateOf(true) } // Abierta por defecto
    var agendaExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¡Hola, ${usuario?.nomYapeUsuario ?: "Cargando..."}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Hoy es un buen día para cuidarte",
                            style = MaterialTheme.typography.bodySmall,
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
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "S.O.S")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // TARJETA 1: GLUCOSA (Interactiva)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                    .clickable { glucosaExpanded = !glucosaExpanded },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = "Glucosa", tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Última Glucosa", fontWeight = FontWeight.Bold)
                        }
                        Icon(if (glucosaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                    }

                    if (glucosaExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text("110 mg/dL", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Text("Rango Saludable", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                            Text("Hace 15 min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = 0.5f, modifier = Modifier.fillMaxWidth(), color = Color.Green)
                    }
                }
            }

            // TARJETA 2: CUMPLIMIENTO (Interactiva)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { cumplimientoExpanded = !cumplimientoExpanded },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cumplimiento Diario", fontWeight = FontWeight.Bold)
                        Icon(if (cumplimientoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                    }

                    if (cumplimientoExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Agua (1.5L / 2L)", style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = 0.75f, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color.Cyan)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Dieta (Almuerzo pendiente)", style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = 0.5f, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color.Green)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Actividad Física (Completada)", style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = 1.0f, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color.Magenta)
                    } else {
                        Text("Dieta: 50% | Ejercicio: 100% | Agua: 75%", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            // TARJETA 3: PRÓXIMA ACTIVIDAD (Agenda - Interactiva)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { agendaExpanded = !agendaExpanded },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = "Agenda", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Próxima Actividad", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Icon(if (agendaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Hora", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("13:00 - Almuerzo", fontWeight = FontWeight.SemiBold)
                    }
                    Text("Ensalada de Quinoa y Pollo", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 24.dp, top = 2.dp))

                    if (agendaExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = "Hora", modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("17:30 - Entrenamiento", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        }
                        Text("Aún no registraste tu rutina de hoy.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 24.dp, top = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Espacio para que el FAB no tape la última tarjeta
        }
    }
}