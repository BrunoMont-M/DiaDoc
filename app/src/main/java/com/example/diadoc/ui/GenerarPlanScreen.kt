package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutricionista IA") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                    Text("Analizando tu perfil médico...", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Cruzando datos de alergias y patologías con el catálogo de alimentos para armar tu dieta de hoy.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is Resource.Success -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = Color(0xFF4CAF50), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("¡Plan Generado con Éxito!", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF4CAF50))
                    Text(
                        "La IA ha diseñado un menú seguro y adaptado a tus necesidades clínicas.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Volver al Dashboard")
                    }
                }
                is Resource.Error -> {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Ocurrió un problema", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red)
                    Text(
                        (generacionState as Resource.Error).message ?: "Error desconocido",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    Button(onClick = { viewModel.generarPlanParaUsuario(uid) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Intentar Nuevamente")
                    }
                }
                else -> {
                    // Estado Inicial
                    Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Diseña tu Día con IA", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        "El motor analizará tu IMC, patologías y restricciones para crear un plan nutricional seguro y personalizado.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    Button(
                        onClick = { viewModel.generarPlanParaUsuario(uid) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERAR PLAN AHORA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}