package com.example.diadoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.RecetaPersonalizada
import com.example.diadoc.viewmodel.RecetarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetarioScreen(
    uid: String,
    viewModel: RecetarioViewModel,
    onNavigateBack: () -> Unit
) {
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary

    val categorias = listOf("Todas", "Desayuno", "Almuerzo", "Merienda", "Cena", "Snacks")

    LaunchedEffect(uid) {
        viewModel.cargarRecetas(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recetario Global", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar receta o ingrediente...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor
                ),
                singleLine = true
            )

            // Pestañas (Tabs) de Categorías
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    FilterChip(
                        selected = categoriaSeleccionada == cat,
                        onClick = { viewModel.cambiarCategoria(cat, uid) },
                        label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryColor,
                            containerColor = cardColor,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = categoriaSeleccionada == cat,
                            borderColor = if (categoriaSeleccionada == cat) primaryColor else Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido Principal
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (recetas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (searchQuery.isNotEmpty()) "No hay resultados para tu búsqueda." else "Aún no tienes recetas.",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchQuery.isEmpty()) {
                        Text(
                            "Guarda platos de tu IA o crea los tuyos.",
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recetas, key = { it.codReceta }) { receta ->
                        TarjetaRecetaAvanzada(
                            receta = receta,
                            cardColor = cardColor,
                            primaryColor = primaryColor,
                            onToggleFavorito = { viewModel.alternarFavorito(receta, uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaRecetaAvanzada(
    receta: RecetaPersonalizada,
    cardColor: Color,
    primaryColor: Color,
    onToggleFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Icono, Título y Botón Favorito
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = primaryColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receta.nombreReceta.ifEmpty { "Receta sin nombre" },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = receta.tipoComida,
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (receta.esFavorita) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorito",
                        tint = if (receta.esFavorita) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Descripción e Instrucciones
            Text(
                text = receta.instruccionesReceta.ifEmpty { "Sin instrucciones detalladas." },
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Macros
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroBadge(valor = "${receta.kcalTotales.toInt()} kcal", color = Color(0xFFE53935))
                MacroBadge(valor = "${receta.proteinasTotales.toInt()}g Prot", color = Color(0xFF4CAF50))
                MacroBadge(valor = "${receta.carbohidratosTotales.toInt()}g Carb", color = Color(0xFF29B6F6))
            }
        }
    }
}

@Composable
fun MacroBadge(valor: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = valor, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}