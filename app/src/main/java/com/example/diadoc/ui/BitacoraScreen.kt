package com.example.diadoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.viewmodel.BitacoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraScreen(
    viewModel: BitacoraViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val progresoHoy by viewModel.progresoHoy.collectAsState()
    val comidasCheckIn by viewModel.comidasCheckIn.collectAsState()
    val patologias by viewModel.patologias.collectAsState()
    val historialReciente by viewModel.historialReciente.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var valorInput by remember { mutableStateOf("") }
    var expandedMomento by remember { mutableStateOf(false) }
    var momentoSeleccionado by remember { mutableStateOf("En ayunas") }

    val focusManager = LocalFocusManager.current

    val opcionesMomento = listOf("En ayunas", "Antes de comer", "Después de comer", "Antes de dormir", "Otro")

    val isDiabetico = patologias.contains("diabet")
    val labelBiometria = if (isDiabetico) "Glucosa" else "Peso"
    val unidadBiometria = if (isDiabetico) "mg/dL" else "kg"

    LaunchedEffect(uid) {
        viewModel.cargarBitacora(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bitácora de Salud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PROGRESO DE HOY:", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${(progresoHoy * 100).toInt()}%", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = progresoHoy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CHECK-IN DE ACTIVIDADES", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (comidasCheckIn.isEmpty()) {
                            Text("No hay actividades registradas en tu plan de hoy.", fontStyle = FontStyle.Italic, color = Color.Gray)
                        } else {
                            val ordenCronologico = listOf("Desayuno", "Media Mañana", "Almuerzo", "Media Tarde", "Merienda", "Cena")
                            val comidasOrdenadas = comidasCheckIn.sortedBy { comida ->
                                val index = ordenCronologico.indexOfFirst { it.equals(comida.tipoComida, ignoreCase = true) }
                                if (index != -1) index else 99
                            }

                            comidasOrdenadas.forEach { comida ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleComidaCheckIn(comida.codDetDieta, comida.consumido) }
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = comida.consumido,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = "${comida.tipoComida}: ${comida.nombrePlato.ifEmpty { "Sugerencia del Chef" }} ${if (comida.consumido) "(Hecho)" else ""}",
                                            fontWeight = if (comida.consumido) FontWeight.Bold else FontWeight.Normal,
                                            color = if (comida.consumido) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${comida.kcalTotales.toInt()} kcal | ${comida.carbohidratosTotales}g Carbs",
                                            fontSize = 12.sp,
                                            color = if (comida.consumido) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Checkbox(checked = false, onCheckedChange = null, enabled = false)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entrenamiento: (Pendiente de generación)", color = Color.Gray)
                        }
                    }
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("REGISTRO BIOMÉTRICO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedMomento,
                            onExpandedChange = { expandedMomento = !expandedMomento }
                        ) {
                            OutlinedTextField(
                                value = momentoSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Momento") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMomento) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMomento,
                                onDismissRequest = { expandedMomento = false }
                            ) {
                                opcionesMomento.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            momentoSeleccionado = opcion
                                            expandedMomento = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = valorInput,
                                onValueChange = { valorInput = it },
                                label = { Text(labelBiometria) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(unidadBiometria, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.guardarMedicion(uid, valorInput, momentoSeleccionado)
                                valorInput = ""
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = valorInput.isNotEmpty() && !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("GUARDAR MEDICIÓN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Text("HISTORIAL RECIENTE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (historialReciente.isEmpty()) {
                item {
                    Text("Aún no tienes registros hoy.", modifier = Modifier.padding(horizontal = 8.dp), fontStyle = FontStyle.Italic, color = Color.Gray)
                }
            } else {
                items(historialReciente) { registro ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(registro.hora, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(50.dp))

                        HorizontalDivider(
                            modifier = Modifier.width(2.dp).height(16.dp),
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(registro.descripcion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "El valor ingresado se sincronizará con tu reporte médico semanal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}