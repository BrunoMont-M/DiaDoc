package com.example.diadoc.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.PerfilMedico
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilMedicoScreen(
    viewModel: PerfilMedicoViewModel,
    codUsuarioLogueado: String,
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val saveState by viewModel.saveState.collectAsState()
    val patologias by viewModel.patologias.collectAsState()
    val restricciones by viewModel.restricciones.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val perfilExistente by viewModel.perfilExistente.collectAsState()
    val patologiasPrevias by viewModel.patologiasPrevias.collectAsState()
    val restriccionesPrevias by viewModel.restriccionesPrevias.collectAsState()

    // Preferencias de unidades
    val usarKg by viewModel.usarKg.collectAsState()
    val usarCm by viewModel.usarCm.collectAsState()

    val refreshState = rememberPullToRefreshState()

    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.cargarDatosIniciales(codUsuarioLogueado)
            delay(500)
            refreshState.endRefresh()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales(codUsuarioLogueado)
    }

    var fechaNacimiento by remember { mutableStateOf("") }
    var pesoActual by remember { mutableStateOf("") }
    var alturaPerfil by remember { mutableStateOf("") }
    var alergias by remember { mutableStateOf("") }
    var grupoSanguineo by remember { mutableStateOf("") }
    var expandedSangre by remember { mutableStateOf(false) }
    val opcionesSangre = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    // Estados para Sexo Biológico
    var sexoBiologico by remember { mutableStateOf("") }
    var expandedSexo by remember { mutableStateOf(false) }
    val opcionesSexo = listOf("Femenino", "Masculino", "Otro")

    val patologiasSeleccionadas = remember { mutableStateListOf<String>() }
    val restriccionesSeleccionadas = remember { mutableStateListOf<String>() }

    var checkOtraPatologia by remember { mutableStateOf(false) }
    var nuevaPatologiaTexto by remember { mutableStateOf("") }
    var checkOtraRestriccion by remember { mutableStateOf(false) }
    var nuevaRestriccionTexto by remember { mutableStateOf("") }

    LaunchedEffect(usuario) {
        if (fechaNacimiento.isEmpty()) fechaNacimiento = usuario?.fechaNacimiento ?: ""
    }

    LaunchedEffect(perfilExistente, usarKg, usarCm) {
        perfilExistente?.let {
            val pesoMostrar = if (usarKg) it.pesoActual else it.pesoActual * 2.20462
            pesoActual = if (pesoMostrar > 0) String.format(java.util.Locale.US, "%.1f", pesoMostrar) else ""

            val alturaMostrar = if (usarCm) it.alturaPerfil else it.alturaPerfil / 2.54
            alturaPerfil = if (alturaMostrar > 0) String.format(java.util.Locale.US, "%.1f", alturaMostrar) else ""

            grupoSanguineo = it.grupoSanguineo
            alergias = it.alergias

            if (it.sexoBiologico.isNotEmpty()) {
                sexoBiologico = it.sexoBiologico
            }
        }
    }

    LaunchedEffect(patologiasPrevias, patologias) {
        if (patologias.isNotEmpty() && patologiasPrevias.isNotEmpty()) {
            patologiasSeleccionadas.clear()
            patologiasPrevias.forEach { previo ->
                val pat = patologias.find { it.nombreEnfermedad == previo || it.codPatologia == previo }
                if (pat != null) {
                    patologiasSeleccionadas.add(pat.codPatologia)
                } else {
                    checkOtraPatologia = true
                    nuevaPatologiaTexto = previo
                }
            }
        }
    }

    LaunchedEffect(restriccionesPrevias, restricciones) {
        if (restricciones.isNotEmpty() && restriccionesPrevias.isNotEmpty()) {
            restriccionesSeleccionadas.clear()
            restriccionesPrevias.forEach { previo ->
                val res = restricciones.find { it.nombreRestricc == previo || it.codRestricc == previo }
                if (res != null) {
                    restriccionesSeleccionadas.add(res.codRestricc)
                } else {
                    checkOtraRestriccion = true
                    nuevaRestriccionTexto = previo
                }
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fechaNacimiento = "${dayOfMonth}/${month + 1}/${year}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(saveState) {
        if (saveState is Resource.Success) {
            Toast.makeText(context, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
            onNavigateNext()
        } else if (saveState is Resource.Error) {
            Toast.makeText(context, (saveState as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetSaveState()
        }
    }

    val labelPeso = if (usarKg) "Peso (kg)" else "Peso (lb)"
    val labelAltura = if (usarCm) "Altura (cm)" else "Altura (in)"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Configurar Perfil Médico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Esta información es vital para generar tu plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
                )

                Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        label = { Text("Fecha de Nacimiento") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, "Fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Sexo Biológico
                ExposedDropdownMenuBox(
                    expanded = expandedSexo,
                    onExpandedChange = { expandedSexo = it }
                ) {
                    OutlinedTextField(
                        value = sexoBiologico,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sexo Biológico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSexo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSexo,
                        onDismissRequest = { expandedSexo = false }
                    ) {
                        opcionesSexo.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    sexoBiologico = opcion
                                    expandedSexo = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = pesoActual,
                        onValueChange = { pesoActual = it },
                        label = { Text(labelPeso) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = alturaPerfil,
                        onValueChange = { alturaPerfil = it },
                        label = { Text(labelAltura) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                val pesoNum = pesoActual.replace(",", ".").toDoubleOrNull() ?: 0.0
                val alturaNum = alturaPerfil.replace(",", ".").toDoubleOrNull() ?: 0.0
                if (pesoNum > 0 && alturaNum > 0) {
                    // Normalizamos a métrico temporalmente solo para el cálculo matemático del IMC
                    val pesoMetrico = if (usarKg) pesoNum else pesoNum / 2.20462
                    val alturaMetrica = if (usarCm) alturaNum else alturaNum * 2.54

                    val alturaMetros = alturaMetrica / 100
                    val imc = pesoMetrico / (alturaMetros * alturaMetros)
                    val imcLabel = when {
                        imc < 18.5 -> "(Bajo peso)"
                        imc in 18.5..24.9 -> "(Saludable)"
                        imc in 25.0..29.9 -> "(Sobrepeso)"
                        else -> "(Obesidad)"
                    }
                    Text(
                        text = "IMC Calculado: ${String.format(java.util.Locale.US, "%.1f", imc)} $imcLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSangre,
                    onExpandedChange = { expandedSangre = it }
                ) {
                    OutlinedTextField(
                        value = grupoSanguineo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grupo Sanguíneo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSangre) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSangre,
                        onDismissRequest = { expandedSangre = false }
                    ) {
                        opcionesSangre.forEach { opcion ->
                            DropdownMenuItem(text = { Text(opcion) }, onClick = { grupoSanguineo = opcion; expandedSangre = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Patologías Base", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                if (patologias.isEmpty()) {
                    Text("Cargando opciones o ninguna registrada...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
                }
                patologias.forEach { patologia ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = patologiasSeleccionadas.contains(patologia.codPatologia),
                            onCheckedChange = { isChecked ->
                                if (isChecked) patologiasSeleccionadas.add(patologia.codPatologia)
                                else patologiasSeleccionadas.remove(patologia.codPatologia)
                            }
                        )
                        Text(text = patologia.nombreEnfermedad, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = checkOtraPatologia, onCheckedChange = { checkOtraPatologia = it })
                    Text(text = "Otra patología no listada...", style = MaterialTheme.typography.bodyMedium)
                }
                if (checkOtraPatologia) {
                    OutlinedTextField(
                        value = nuevaPatologiaTexto,
                        onValueChange = { nuevaPatologiaTexto = it },
                        label = { Text("Especifique la patología") },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Restricciones Alimentarias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                if (restricciones.isEmpty()) {
                    Text("Cargando opciones o ninguna registrada...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
                }
                restricciones.forEach { restriccion ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = restriccionesSeleccionadas.contains(restriccion.codRestricc),
                            onCheckedChange = { isChecked ->
                                if (isChecked) restriccionesSeleccionadas.add(restriccion.codRestricc)
                                else restriccionesSeleccionadas.remove(restriccion.codRestricc)
                            }
                        )
                        Text(text = restriccion.nombreRestricc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = checkOtraRestriccion, onCheckedChange = { checkOtraRestriccion = it })
                    Text(text = "Otra restricción no listada...", style = MaterialTheme.typography.bodyMedium)
                }
                if (checkOtraRestriccion) {
                    OutlinedTextField(
                        value = nuevaRestriccionTexto,
                        onValueChange = { nuevaRestriccionTexto = it },
                        label = { Text("Especifique la restricción") },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = alergias,
                    onValueChange = { alergias = it },
                    label = { Text("Alergias u observaciones adicionales") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (saveState is Resource.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    DiaDocButton(
                        text = "GUARDAR CAMBIOS",
                        icon = rememberVectorPainter(Icons.Default.Save),
                        onClick = {
                            val perfilFormulario = PerfilMedico(
                                codPerfil = perfilExistente?.codPerfil ?: "",
                                codUsuario = codUsuarioLogueado,
                                pesoActual = pesoNum,
                                alturaPerfil = alturaNum,
                                grupoSanguineo = grupoSanguineo,
                                alergias = alergias,
                                sexoBiologico = sexoBiologico
                            )
                            viewModel.guardarPerfilCompleto(
                                perfil = perfilFormulario,
                                fechaNacimiento = fechaNacimiento,
                                patologiasSeleccionadas = patologiasSeleccionadas,
                                restriccionesSeleccionadas = restriccionesSeleccionadas,
                                nuevaPatologiaTexto = if (checkOtraPatologia) nuevaPatologiaTexto else "",
                                nuevaRestriccionTexto = if (checkOtraRestriccion) nuevaRestriccionTexto else ""
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
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
    }
}