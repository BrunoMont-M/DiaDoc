package com.example.diadoc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diadoc.viewmodel.UnidadesMedidaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesMedidaScreen(
    viewModel: UnidadesMedidaViewModel,
    onNavigateBack: () -> Unit
) {
    val usarMgdl by viewModel.usarMgdl.collectAsState()
    val usarKg by viewModel.usarKg.collectAsState()
    val usarCm by viewModel.usarCm.collectAsState()
    val usarMl by viewModel.usarMl.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unidades de Medida", style = MaterialTheme.typography.titleLarge) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personaliza cómo quieres visualizar tus registros. Esto no afectará tu historial guardado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Glucosa
            UnidadSelectorCard(
                titulo = "Glucosa en Sangre",
                opcionA = "mg/dL (Estándar Latam)",
                opcionB = "mmol/L (Internacional)",
                seleccionEsA = usarMgdl,
                onSeleccionChange = { viewModel.actualizarMgdl(it) }
            )

            // Peso
            UnidadSelectorCard(
                titulo = "Peso Corporal",
                opcionA = "Kilogramos (kg)",
                opcionB = "Libras (lb)",
                seleccionEsA = usarKg,
                onSeleccionChange = { viewModel.actualizarKg(it) }
            )

            // Altura
            UnidadSelectorCard(
                titulo = "Estatura",
                opcionA = "Centímetros (cm)",
                opcionB = "Pies / Pulgadas (ft/in)",
                seleccionEsA = usarCm,
                onSeleccionChange = { viewModel.actualizarCm(it) }
            )

            // Líquidos
            UnidadSelectorCard(
                titulo = "Hidratación y Líquidos",
                opcionA = "Mililitros (ml)",
                opcionB = "Onzas (oz)",
                seleccionEsA = usarMl,
                onSeleccionChange = { viewModel.actualizarMl(it) }
            )
        }
    }
}

@Composable
fun UnidadSelectorCard(
    titulo: String,
    opcionA: String,
    opcionB: String,
    seleccionEsA: Boolean,
    onSeleccionChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSeleccionChange(true) }
            ) {
                RadioButton(selected = seleccionEsA, onClick = { onSeleccionChange(true) })
                Text(text = opcionA, modifier = Modifier.padding(start = 8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSeleccionChange(false) }
            ) {
                RadioButton(selected = !seleccionEsA, onClick = { onSeleccionChange(false) })
                Text(text = opcionB, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}