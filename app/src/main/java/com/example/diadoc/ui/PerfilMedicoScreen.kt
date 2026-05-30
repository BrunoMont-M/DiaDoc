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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.model.PerfilMedico
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilMedicoScreen(
    viewModel: PerfilMedicoViewModel,
    codUsuarioLogueado: String,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val saveState by viewModel.saveState.collectAsState()
    val patologias by viewModel.patologias.collectAsState()
    val restricciones by viewModel.restricciones.collectAsState()
    val usuario by viewModel.usuario.collectAsState()

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

    val patologiasSeleccionadas = remember { mutableStateListOf<String>() }
    val restriccionesSeleccionadas = remember { mutableStateListOf<String>() }

    // Sincronizar fecha al cargar de Firebase si ya tuviese una
    LaunchedEffect(usuario) {
        if (fechaNacimiento.isEmpty()) fechaNacimiento = usuario?.fechaNacimiento ?: ""
    }

    // Configuración del Calendario Nativo
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
            Toast.makeText(context, "Perfil completo guardado", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
            onNavigateNext()
        } else if (saveState is Resource.Error) {
            Toast.makeText(context, (saveState as Resource.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetSaveState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Configurar Perfil Médico",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Esta información es vital para generar tu plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, bottom = 24.dp)
        )

        // 1. Campo Fecha clickeable (Abre el calendario y no el teclado)
        Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { },
                readOnly = true,
                enabled = false, // Impide que se abra el teclado
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

        // 2. Datos Biométricos
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = pesoActual,
                onValueChange = { pesoActual = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = alturaPerfil,
                onValueChange = { alturaPerfil = it },
                label = { Text("Altura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // LÓGICA DEL CÁLCULO DE IMC EN TIEMPO REAL
        val pesoNum = pesoActual.replace(",", ".").toDoubleOrNull() ?: 0.0
        val alturaNum = alturaPerfil.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (pesoNum > 0 && alturaNum > 0) {
            val alturaMetros = alturaNum / 100
            val imc = pesoNum / (alturaMetros * alturaMetros)
            val imcLabel = when {
                imc < 18.5 -> "(Bajo peso)"
                imc in 18.5..24.9 -> "(Saludable)"
                imc in 25.0..29.9 -> "(Sobrepeso)"
                else -> "(Obesidad)"
            }
            Text(
                text = "IMC Calculado: ${String.format("%.1f", imc)} $imcLabel",
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

        // 3. Patologías Base
        Text("Patologías Base", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        patologias.forEach { patologia ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = patologiasSeleccionadas.contains(patologia.codPatologia),
                    onCheckedChange = { isChecked ->
                        if (isChecked) patologiasSeleccionadas.add(patologia.codPatologia)
                        else patologiasSeleccionadas.remove(patologia.codPatologia)
                    }
                )
                Text(text = patologia.nombreEnfermedad)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Restricciones Alimentarias
        Text("Restricciones Alimentarias", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        restricciones.forEach { restriccion ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = restriccionesSeleccionadas.contains(restriccion.codRestricc),
                    onCheckedChange = { isChecked ->
                        if (isChecked) restriccionesSeleccionadas.add(restriccion.codRestricc)
                        else restriccionesSeleccionadas.remove(restriccion.codRestricc)
                    }
                )
                Text(text = restriccion.nombreRestricc)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Alergias Adicionales
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
            Button(
                onClick = {
                    val perfilNuevo = PerfilMedico(
                        codUsuario = codUsuarioLogueado,
                        pesoActual = pesoNum,
                        alturaPerfil = alturaNum,
                        grupoSanguineo = grupoSanguineo,
                        alergias = alergias
                    )
                    viewModel.guardarPerfilCompleto(perfilNuevo, fechaNacimiento, patologiasSeleccionadas, restriccionesSeleccionadas)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("GUARDAR Y CONTINUAR", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}