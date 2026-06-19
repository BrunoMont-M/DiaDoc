package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.Alimento
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoAlimentosScreen(
    viewModel: CatalogoAlimentosViewModel,
    onBackClick: () -> Unit = {}
) {
    // Forzamos a que Compose reconozca que es una lista de Alimento desde el Flow
    val listaAlimentos: List<Alimento> by viewModel.alimentos.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarAlimentos()
    }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryBlue = Color(0xFF00668B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Alimentos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryBlue
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Gestión del Catálogo Maestro (Admin)",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (listaAlimentos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay alimentos en el catálogo maestro", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Eliminamos el casteo manual "as Alimento" que rompía la app
                            items(listaAlimentos) { alimento ->
                                TarjetaCatalogoItem(
                                    alimento = alimento,
                                    cardColor = cardColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaCatalogoItem(alimento: Alimento, cardColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = alimento.nombreAlimento, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kcal: ${alimento.kcalBase} | IG: ${alimento.indiceGlucemico}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Text(
                text = "P: ${alimento.proteinasBase}g | C: ${alimento.carbohidratosBase}g | G: ${alimento.grasasBase}g",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}