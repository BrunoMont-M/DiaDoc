package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.GeneradorPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerarPlanScreen(
    viewModel: GeneradorPlanViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val generacionState by viewModel.generacionState.collectAsState()

    val alertGood = DiaDocTheme.colors.alertGood
    val alertDanger = DiaDocTheme.colors.alertDanger
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutricionista IA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (generacionState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Analizando tu perfil médico...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Cruzando datos de alergias y patologías con el catálogo de alimentos para armar tu dieta de hoy.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is Resource.Success -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = alertGood, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("¡Plan Generado con Éxito!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = alertGood)
                    Text(
                        "La IA ha diseñado un menú seguro y adaptado a tus necesidades clínicas.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    DiaDocButton(
                        text = "VOLVER AL DASHBOARD",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
                is Resource.Error -> {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = alertDanger, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Ocurrió un problema", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = alertDanger)
                    Text(
                        (generacionState as Resource.Error).message ?: "Error desconocido",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    DiaDocButton(
                        text = "INTENTAR NUEVAMENTE",
                        onClick = { viewModel.generarPlanParaUsuario(uid) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
                else -> {
                    // Estado Inicial
                    Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Diseña tu Día con IA", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "El motor analizará tu IMC, patologías y restricciones para crear un plan nutricional seguro y personalizado.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    DiaDocButton(
                        text = "GENERAR PLAN AHORA",
                        icon = rememberVectorPainter(Icons.Default.AutoAwesome),
                        onClick = { viewModel.generarPlanParaUsuario(uid) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }
        }
    }
}