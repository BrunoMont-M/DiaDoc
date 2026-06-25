package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.ContactoEmergencia
import com.example.diadoc.viewmodel.ContactosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    viewModel: ContactosViewModel,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val contactos by viewModel.contactos.collectAsState()
    val errorTelefono by viewModel.errorTelefono.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var vinculo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contactoEditando by remember { mutableStateOf<ContactoEmergencia?>(null) }

    LaunchedEffect(uid) {
        viewModel.cargarContactos(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Red de Contención") },
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
                .padding(16.dp)
        ) {
            Text("AÑADIR NUEVO CONTACTO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = vinculo,
                onValueChange = { vinculo = it },
                label = { Text("Vínculo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errorTelefono,
                supportingText = { if (errorTelefono) Text("Ingrese un teléfono válido") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.guardarContacto(uid, nombre, vinculo, telefono, contactoEditando?.codContacto ?: "")
                    if (!errorTelefono) {
                        nombre = ""
                        vinculo = ""
                        telefono = ""
                        contactoEditando = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (contactoEditando != null) "GUARDAR CAMBIOS" else "[+] AGREGAR CONTACTO")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("MIS CONTACTOS DE EMERGENCIA", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contactos) { contacto ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${contacto.nombreContacto} (${contacto.vinculo})", fontWeight = FontWeight.Bold)
                                Text(contacto.telefono, color = Color.Gray)
                            }
                            Row {
                                TextButton(onClick = {
                                    nombre = contacto.nombreContacto
                                    vinculo = contacto.vinculo
                                    telefono = contacto.telefono
                                    contactoEditando = contacto
                                }) { Text("Editar") }
                                TextButton(onClick = { viewModel.eliminarContacto(contacto.codContacto, uid) }) {
                                    Text("Eliminar", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "[i] Estos contactos recibirán un SMS con tu ubicación exacta si presionas el botón S.O.S.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}