package com.example.diadoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutricionMenuScreen(
    onNavigateToPlanNutricional: () -> Unit,
    onNavigateToRegistrarAlimento: () -> Unit,
    onNavigateToCrearReceta: () -> Unit,
    onNavigateToRecetario: () -> Unit
) {
    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulo de Nutrición", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¿Qué deseas hacer hoy?",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta Principal: Plan Nutricional de Hoy
            Card(
                onClick = onNavigateToPlanNutricional,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Mi Plan Nutricional", color = primaryColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Revisar tus comidas y check-in del día.", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }

            Divider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))

            // Tarjeta Opción 1: Registrar Alimento
            Card(
                onClick = onNavigateToRegistrarAlimento,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
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
                        Text("Registrar Alimento", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Carga manual o mediante escaneo de código QR / Barras.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            // Tarjeta Opción 2: Crear Receta
            Card(
                onClick = onNavigateToCrearReceta,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
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
                        Text("Crear Receta", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Armá tus platos personalizados sumando ingredientes y calculando macros.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            // Tarjeta Opción 3: Recetario
            Card(
                onClick = onNavigateToRecetario,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
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
                        Text("Recetario Global", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Explora todas las recetas generadas por IA y creadas por ti.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}