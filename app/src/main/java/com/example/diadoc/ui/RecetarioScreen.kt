package com.example.diadoc.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    var recetaAEliminar by remember { mutableStateOf<RecetaPersonalizada?>(null) }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary

    val categorias = listOf("Todas", "Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena", "Personalizada")

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
                            onToggleFavorito = { viewModel.alternarFavorito(receta, uid) },
                            onDeleteClick = { recetaAEliminar = receta }
                        )
                    }
                }
            }
        }

        if (recetaAEliminar != null) {
            AlertDialog(
                onDismissRequest = { recetaAEliminar = null },
                title = { Text("Eliminar Receta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = { Text("¿Estás seguro que deseas eliminar '${recetaAEliminar?.nombreReceta}' de tu recetario global? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            recetaAEliminar?.let { viewModel.eliminarReceta(it.codReceta, uid) }
                            recetaAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, eliminar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recetaAEliminar = null }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                containerColor = cardColor,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

@Composable
fun TarjetaRecetaAvanzada(
    receta: RecetaPersonalizada,
    cardColor: Color,
    primaryColor: Color,
    onToggleFavorito: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val esRecetaIA = receta.origenIA && receta.instruccionesReceta.contains("|||")
    val partes = if (esRecetaIA) receta.instruccionesReceta.split("|||") else emptyList()
    val descripcionPreview = if (esRecetaIA) partes.getOrNull(0) ?: "" else receta.instruccionesReceta

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
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

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
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

            Text(
                text = if (esRecetaIA) "\"$descripcionPreview\"" else descripcionPreview.ifEmpty { "Sin instrucciones detalladas." },
                fontStyle = if (esRecetaIA) FontStyle.Italic else FontStyle.Normal,
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                maxLines = if (expanded && !esRecetaIA) Int.MAX_VALUE else if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (expanded && esRecetaIA) {
                val ingredientesStr = partes.getOrNull(1) ?: ""
                val pasosStr = partes.getOrNull(2) ?: ""

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ingredientes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                val listaIngredientes = ingredientesStr.split("@@").filter { it.isNotBlank() }
                listaIngredientes.forEach { ing ->
                    val datos = ing.split("::")
                    val nombre = datos.getOrNull(0) ?: ""
                    val kcal = datos.getOrNull(1) ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• $nombre", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.weight(1f).padding(end = 8.dp))
                        if (kcal.isNotEmpty()) {
                            Text("$kcal kcal", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Preparación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                val listaPasos = pasosStr.split("@@").filter { it.isNotBlank() }
                listaPasos.forEachIndexed { index, paso ->
                    Row(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(paso, fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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