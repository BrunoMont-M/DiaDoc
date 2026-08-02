package com.example.diadoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.theme.PrimaryBlueLight
import com.example.diadoc.ui.theme.PrimaryGreenLight
import com.example.diadoc.ui.theme.PrimaryLight
import com.example.diadoc.viewmodel.PersonalizacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizacionScreen(
    viewModel: PersonalizacionViewModel,
    onNavigateBack: () -> Unit
) {
    val temaActual by viewModel.temaApp.collectAsState()
    val paletaActual by viewModel.paletaApp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalización", style = MaterialTheme.typography.titleLarge) },
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

            // SECCIÓN: COLOR PRINCIPAL (PALETA)
            Text(
                text = "Color de Acento",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Elige el color principal de la aplicación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ColorSelector(color = PrimaryLight, seleccionado = paletaActual == 0) { viewModel.cambiarPaleta(0) }
                ColorSelector(color = PrimaryBlueLight, seleccionado = paletaActual == 1) { viewModel.cambiarPaleta(1) }
                ColorSelector(color = PrimaryGreenLight, seleccionado = paletaActual == 2) { viewModel.cambiarPaleta(2) }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SECCIÓN: TEMA CLARO/OSCURO
            Text(
                text = "Apariencia de la aplicación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Puedes sincronizarlo con el sistema o forzar un tema específico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OpcionTemaItem(
                titulo = "Predeterminado del sistema",
                descripcion = "Se adapta al modo de tu dispositivo",
                icono = Icons.Default.BrightnessAuto,
                seleccionado = temaActual == 0,
                onClick = { viewModel.cambiarTema(0) }
            )

            OpcionTemaItem(
                titulo = "Modo Claro",
                descripcion = "Fondo claro con textos oscuros",
                icono = Icons.Default.BrightnessHigh,
                seleccionado = temaActual == 1,
                onClick = { viewModel.cambiarTema(1) }
            )

            OpcionTemaItem(
                titulo = "Modo Oscuro",
                descripcion = "Fondo oscuro para descansar la vista",
                icono = Icons.Default.Brightness4,
                seleccionado = temaActual == 2,
                onClick = { viewModel.cambiarTema(2) }
            )
        }
    }
}

@Composable
fun ColorSelector(color: Color, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .border(
                width = if (seleccionado) 4.dp else 0.dp,
                color = if (seleccionado) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (seleccionado) {
            Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = Color.White)
        }
    }
}

@Composable
fun OpcionTemaItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
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
                tint = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = seleccionado,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}