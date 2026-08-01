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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.RecetaPersonalizada
import com.example.diadoc.ui.theme.DiaDocTheme
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

    // Estado: Filtro Dual (IA vs Manuales)
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mis Recetas", "Sugeridas por IA")

    val categorias = listOf("Todas", "Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena")
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val alertDanger = DiaDocTheme.colors.alertDanger

    LaunchedEffect(uid) {
        viewModel.cargarRecetas(uid)
    }

    // FILTRADO LOCAL REACTIVO
    val recetasFiltradas = recetas.filter { receta ->
        if (tabIndex == 0) !receta.origenIA else receta.origenIA
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recetario Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar receta o ingrediente...", color = onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (tabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = onSurfaceVariant
                    )
                }
            }

            // Pestañas (Chips) de Categorías
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    FilterChip(
                        selected = categoriaSeleccionada == cat,
                        onClick = { viewModel.cambiarCategoria(cat, uid) },
                        label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Contenido Principal
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (recetasFiltradas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No hay resultados para tu búsqueda."
                        } else if (tabIndex == 0) {
                            "Aún no tienes recetas manuales."
                        } else {
                            "Aún no hay sugerencias de IA."
                        },
                        color = onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tabIndex == 0) "Crea tus propias recetas para verlas aquí." else "Genera tu plan diario para obtener sugerencias.",
                            color = onSurfaceVariant.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recetasFiltradas, key = { it.codReceta }) { receta ->
                        TarjetaRecetaAvanzada(
                            receta = receta,
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
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Eliminar Receta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = alertDanger) },
                text = { Text("¿Estás seguro que deseas eliminar '${recetaAEliminar?.nombreReceta}' de tu recetario global? Esta acción no se puede deshacer.", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    Button(
                        onClick = {
                            recetaAEliminar?.let { viewModel.eliminarReceta(it.codReceta, uid) }
                            recetaAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = alertDanger)
                    ) {
                        Text("Sí, eliminar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recetaAEliminar = null }) {
                        Text("Cancelar", color = onSurfaceVariant)
                    }
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = alertDanger) }
            )
        }
    }
}

@Composable
fun TarjetaRecetaAvanzada(
    receta: RecetaPersonalizada,
    onToggleFavorito: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val esRecetaIA = receta.origenIA && receta.instruccionesReceta.contains("|||")
    val partes = if (esRecetaIA) receta.instruccionesReceta.split("|||") else emptyList()
    val descripcionPreview = if (esRecetaIA) partes.getOrNull(0) ?: "" else receta.instruccionesReceta

    val primaryColor = MaterialTheme.colorScheme.primary
    val alertDanger = DiaDocTheme.colors.alertDanger
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = receta.tipoComida,
                        color = primaryColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = alertDanger.copy(alpha = 0.8f))
                }

                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (receta.esFavorita) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorito",
                        tint = if (receta.esFavorita) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (esRecetaIA) "\"$descripcionPreview\"" else descripcionPreview.ifEmpty { "Sin instrucciones detalladas." },
                fontStyle = if (esRecetaIA) FontStyle.Italic else FontStyle.Normal,
                color = onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded && !esRecetaIA) Int.MAX_VALUE else if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (expanded && esRecetaIA) {
                val ingredientesStr = partes.getOrNull(1) ?: ""
                val pasosStr = partes.getOrNull(2) ?: ""

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                        Text("• $nombre", style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant, modifier = Modifier.weight(1f).padding(end = 8.dp))
                        if (kcal.isNotEmpty()) {
                            Text("$kcal kcal", color = onSurfaceVariant.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Preparación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                val listaPasos = pasosStr.split("@@").filter { it.isNotBlank() }
                listaPasos.forEachIndexed { index, paso ->
                    Row(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = primaryColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(paso, style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant, modifier = Modifier.weight(1f))
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
        Text(text = valor, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}