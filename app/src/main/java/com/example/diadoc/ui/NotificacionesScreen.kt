package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diadoc.viewmodel.NotificacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel,
    onNavigateBack: () -> Unit
) {
    val notifComidas by viewModel.notifComidas.collectAsState()
    val notifRutinas by viewModel.notifRutinas.collectAsState()
    val notifSistema by viewModel.notifSistema.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones y Alertas", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Controla qué tipo de avisos quieres recibir en tu dispositivo para mantenerte al día con tu plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            SwitchNotificacionItem(
                titulo = "Recordatorios de Comidas",
                descripcion = "Avisos para tus desayunos, almuerzos y cenas según tu plan nutricional.",
                icono = Icons.Default.Restaurant,
                checked = notifComidas,
                onCheckedChange = { viewModel.toggleNotifComidas(it) }
            )

            SwitchNotificacionItem(
                titulo = "Alertas de Rutina y Ejercicio",
                descripcion = "Recordatorios para hacer actividad física o tomar descansos activos.",
                icono = Icons.Default.DirectionsRun,
                checked = notifRutinas,
                onCheckedChange = { viewModel.toggleNotifRutinas(it) }
            )

            SwitchNotificacionItem(
                titulo = "Notificaciones del Sistema",
                descripcion = "Avisos sobre tu cuenta, actualizaciones y recordatorios de S.O.S.",
                icono = Icons.Default.Info,
                checked = notifSistema,
                onCheckedChange = { viewModel.toggleNotifSistema(it) }
            )
        }
    }
}

@Composable
fun SwitchNotificacionItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}