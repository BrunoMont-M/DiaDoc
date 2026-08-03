package com.example.diadoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.PlanNutricionalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutricionMenuScreen(
    viewModel: PlanNutricionalViewModel, // Nuevo parámetro
    uid: String, // Nuevo parámetro
    onNavigateToPlanNutricional: () -> Unit,
    onNavigateToGeneradorIA: () -> Unit, // Nuevo parámetro para la navegación
    onNavigateToRegistrarAlimento: () -> Unit,
    onNavigateToCrearReceta: () -> Unit,
    onNavigateToRecetario: () -> Unit
) {
    // Usamos los colores semánticos del sistema
    val moduleNutrition = DiaDocTheme.colors.moduleNutrition
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Observamos el estado del plan para saber si ya existe uno hoy
    val dietaState by viewModel.dietaState.collectAsState()

    // Cargamos la dieta al entrar a la pantalla para verificar
    LaunchedEffect(uid) {
        viewModel.cargarDietaDeHoy(uid)
    }

    val existePlanHoy = dietaState is Resource.Success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulo de Nutrición", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¿Qué deseas hacer hoy?",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta Dinámica: Generar Plan o Ver Plan
            ElevatedCard(
                onClick = if (existePlanHoy) onNavigateToPlanNutricional else onNavigateToGeneradorIA,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (existePlanHoy) moduleNutrition.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (existePlanHoy) Icons.Default.RestaurantMenu else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (existePlanHoy) moduleNutrition else primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (existePlanHoy) "Mi Plan Nutricional" else "Generar Plan con IA",
                            color = if (existePlanHoy) moduleNutrition else primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (existePlanHoy) "Revisar tus comidas y check-in del día." else "Crea tu menú de hoy adaptado a tus métricas.",
                            color = onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(bottom = 16.dp))

            // Tarjeta Opción 1: Registrar Alimento
            ElevatedCard(
                onClick = onNavigateToRegistrarAlimento,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = surfaceColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Registrar Alimento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Carga manual o mediante escaneo de código QR / Barras.", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Tarjeta Opción 2: Crear Receta
            ElevatedCard(
                onClick = onNavigateToCrearReceta,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = surfaceColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Blender,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Crear Receta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Armá tus platos personalizados sumando ingredientes y calculando macros.", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Tarjeta Opción 3: Recetario
            ElevatedCard(
                onClick = onNavigateToRecetario,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = surfaceColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Recetario Global", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Explora todas las recetas generadas por IA y creadas por ti.", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}