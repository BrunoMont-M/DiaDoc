package com.example.diadoc.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.PlanNutricionalViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanNutricionalScreen(
    viewModel: PlanNutricionalViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val dietaState by viewModel.dietaState.collectAsState()
    val alertaRestriccion by viewModel.alertaRestriccion.collectAsState()

    var editandoCodDetDieta by remember { mutableStateOf<String?>(null) }

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.cargarDietaDeHoy(uid)
            delay(500)
            refreshState.endRefresh()
        }
    }

    LaunchedEffect(uid) {
        viewModel.cargarDietaDeHoy(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(refreshState.nestedScrollConnection)
        ) {
            when (dietaState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (dietaState as Resource.Error).message ?: "Aún no has generado tu plan de hoy.",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is Resource.Success -> {
                    val datos = (dietaState as Resource.Success).data
                    val dieta = datos.first
                    val menuCompleto = datos.second

                    if (editandoCodDetDieta != null) {
                        val entradaEdicion = menuCompleto.entries.find { it.key.codDetDieta == editandoCodDetDieta }
                        if (entradaEdicion != null) {
                            ModalBottomSheet(onDismissRequest = { editandoCodDetDieta = null; viewModel.limpiarCatalogo() }) {
                                EditorDietaContenido(
                                    detalle = entradaEdicion.key,
                                    alimentos = entradaEdicion.value,
                                    viewModel = viewModel,
                                    uid = uid,
                                    codDieta = dieta.codDieta
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Sumatoria explícita para Doubles mapeados
                        val totalKcal: Double = menuCompleto.values.flatten().sumOf { alim: Alimento -> alim.kcalBase }
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Resumen Energético", fontWeight = FontWeight.Bold)
                                Text("${totalKcal.toInt()} Kcal", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        val ordenCronologico = listOf("Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena", "Snack Media Tarde")
                        val menuOrdenado = menuCompleto.entries.sortedBy { entrada ->
                            val index = ordenCronologico.indexOf(entrada.key.tipoComida)
                            if (index != -1) index else 99
                        }

                        menuOrdenado.forEach { entrada ->
                            TarjetaRecetaInteractiva(
                                detalle = entrada.key,
                                alimentos = entrada.value,
                                onToggleConsumido = {
                                    viewModel.alternarConsumoComida(
                                        codPlan = dieta.codPlan,
                                        codDieta = dieta.codDieta,
                                        uid = uid,
                                        codDetDieta = entrada.key.codDetDieta,
                                        consumidoActual = entrada.key.consumido
                                    )
                                },
                                onEditClick = { editandoCodDetDieta = entrada.key.codDetDieta }
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                null -> { }
            }

            if (refreshState.progress > 0f || refreshState.isRefreshing) {
                PullToRefreshContainer(
                    state = refreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (alertaRestriccion != null) {
            AlertDialog(
                onDismissRequest = { viewModel.limpiarAlerta() },
                title = { Text("Operación Bloqueada", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = { Text(alertaRestriccion ?: "") },
                confirmButton = { TextButton(onClick = { viewModel.limpiarAlerta() }) { Text("Entendido") } },
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }
}

@Composable
fun EditorDietaContenido(
    detalle: DetalleDieta,
    alimentos: List<Alimento>,
    viewModel: PlanNutricionalViewModel,
    uid: String,
    codDieta: String
) {
    var searchQuery by remember { mutableStateOf("") }
    val resultados by viewModel.catalogoResultados.collectAsState()

    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSearchFocused) {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    isSearchFocused = false
                    searchQuery = ""
                    viewModel.limpiarCatalogo()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
            Text(
                text = if (isSearchFocused) "Buscando Alimento..." else "Editando: ${detalle.tipoComida}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isSearchFocused) {
            Text("Ingredientes Actuales", fontWeight = FontWeight.SemiBold)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                alimentos.forEach { alim ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${alim.nombreAlimento}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Text("${alim.kcalBase.toInt()} kcal", color = Color.Gray, fontSize = 14.sp)
                        IconButton(onClick = { viewModel.eliminarAlimento(codDieta, uid, detalle.codDetDieta, alim.codAlimento) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.buscarEnCatalogo(it) },
            label = { Text("Buscar en catálogo...") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isSearchFocused = focusState.isFocused
                }, // CORRECCIÓN CLAVE: onFocusChanged correctamente enganchado en el Modifier
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isSearchFocused || searchQuery.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(resultados, key = { it.codAlimento }) { alim: Alimento ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.agregarAlimento(codDieta, uid, detalle.codDetDieta, alim)
                                searchQuery = ""
                                focusManager.clearFocus()
                                isSearchFocused = false
                                viewModel.limpiarCatalogo()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(alim.nombreAlimento, fontWeight = FontWeight.Medium)
                            Text("Proteínas: ${alim.proteinasBase.toInt()}g | Carbs: ${alim.carbohidratosBase.toInt()}g", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("${alim.kcalBase.toInt()} kcal", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TarjetaRecetaInteractiva(
    detalle: DetalleDieta,
    alimentos: List<Alimento>,
    onToggleConsumido: () -> Unit,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val consumido = detalle.consumido

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = "Foto de receta",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detalle.tipoComida.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Opciones de receta",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir/Contraer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = detalle.nombrePlato,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"${detalle.descripcionPlato}\"",
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                if (expanded) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Ingredientes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    alimentos.forEach { alimento ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ${alimento.nombreAlimento}",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = "${alimento.kcalBase.toInt()} kcal",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Preparación", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    detalle.preparacion.forEachIndexed { index, paso ->
                        Row(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(paso, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onToggleConsumido,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (consumido) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (consumido) Icons.Default.Check else Icons.Default.DoneOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (consumido) "Comida Consumida" else "Marcar como Consumido", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}