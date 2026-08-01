package com.example.diadoc.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.ReporteProgresoViewModel
import com.example.diadoc.utils.PdfManager
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteProgresoScreen(
    viewModel: ReporteProgresoViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val tendenciaGlucosa by viewModel.tendenciaGlucosa.collectAsState()
    val promedioGlucosa by viewModel.promedioGlucosa.collectAsState()
    val adherenciaDieta by viewModel.adherenciaDieta.collectAsState()
    val adherenciaEjercicio by viewModel.adherenciaEjercicio.collectAsState()
    val totalRegistros by viewModel.totalRegistros.collectAsState()

    val calendar = Calendar.getInstance()
    var fechaDesdeStr by remember { mutableStateOf("") }
    var fechaDesdeMilis by remember { mutableStateOf<Long?>(null) }
    var fechaHastaStr by remember { mutableStateOf("") }
    var fechaHastaMilis by remember { mutableStateOf<Long?>(null) }

    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val alertDanger = DiaDocTheme.colors.alertDanger

    val datePickerDesde = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            fechaDesdeMilis = cal.timeInMillis
            fechaDesdeStr = format.format(cal.time)
            viewModel.limpiarError()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val datePickerHasta = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 23, 59, 59)
            fechaHastaMilis = cal.timeInMillis
            fechaHastaStr = format.format(cal.time)
            viewModel.limpiarError()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes y Estadísticas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- BLOQUE 1: SELECCIÓN DE FECHAS ---
            Text("FILTRAR POR FECHAS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clickable { datePickerDesde.show() }) {
                    OutlinedTextField(
                        value = fechaDesdeStr,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Desde") },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                            disabledLabelColor = onSurfaceVariant
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f).clickable { datePickerHasta.show() }) {
                    OutlinedTextField(
                        value = fechaHastaStr,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Hasta") },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                            disabledLabelColor = onSurfaceVariant
                        )
                    )
                }
            }

            DiaDocButton(
                text = "GENERAR REPORTE",
                onClick = { viewModel.generarReporte(uid, fechaDesdeMilis, fechaHastaMilis) },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = alertDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(error!!, color = alertDanger, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // --- BLOQUE 2: RESULTADOS Y GRÁFICOS ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (totalRegistros > 0 || adherenciaDieta > 0f) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("TENDENCIA DE GLUCEMIA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = onSurfaceVariant)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("mg/dL", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, modifier = Modifier.align(Alignment.End))
                        Spacer(modifier = Modifier.height(8.dp))

                        if (tendenciaGlucosa.size > 1) {
                            val chartEntryModel = entryModelOf(*tendenciaGlucosa.toTypedArray())
                            Chart(
                                chart = lineChart(
                                    lines = listOf(lineSpec(lineColor = alertDanger))
                                ),
                                model = chartEntryModel,
                                modifier = Modifier.height(150.dp).fillMaxWidth()
                            )
                        } else if (tendenciaGlucosa.size == 1) {
                            Text("Valor único registrado: ${tendenciaGlucosa[0]} mg/dL. Registra más datos para ver la tendencia.", color = onSurfaceVariant, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("No hay datos de glucosa en este rango.", color = onSurfaceVariant, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("RESUMEN DE ADHERENCIA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = onSurfaceVariant)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dieta: ${(adherenciaDieta * 100).toInt()}% Cumplido", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Ejercicios: ${(adherenciaEjercicio * 100).toInt()}% Hecho", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Promedio: ${promedioGlucosa.toInt()} mg/dL", color = alertDanger, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Registros Totales: $totalRegistros", style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        PdfManager.generarReporteProgresoPDF(
                            context = context,
                            fechaDesde = fechaDesdeStr,
                            fechaHasta = fechaHastaStr,
                            promedioGlucosa = promedioGlucosa,
                            adherenciaDieta = adherenciaDieta,
                            adherenciaEjercicio = adherenciaEjercicio,
                            totalRegistros = totalRegistros
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, alertDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = alertDanger)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DESCARGAR COMO PDF", fontWeight = FontWeight.Bold)
                }
            } else if (fechaDesdeMilis != null && fechaHastaMilis != null && !isLoading && error == null) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron registros en el rango seleccionado.", color = onSurfaceVariant, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}