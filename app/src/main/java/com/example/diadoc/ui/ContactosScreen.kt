package com.example.diadoc.ui

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.diadoc.model.ContactoEmergencia
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
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

    val context = LocalContext.current

    // Lanzador para abrir la agenda de contactos nativa y obtener el teléfono seleccionado
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                        if (numberIndex != -1) {
                            // Extrae el número y limpia caracteres extraños (ej: espacios, guiones)
                            val numeroCrudo = it.getString(numberIndex) ?: ""
                            telefono = numeroCrudo.replace(Regex("[^0-9+]"), "")
                        }
                        if (nameIndex != -1 && nombre.isBlank()) {
                            // Si el campo de nombre estaba vacío, lo autocompleta
                            nombre = it.getString(nameIndex) ?: ""
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(uid) {
        viewModel.cargarContactos(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Red de Contención", style = MaterialTheme.typography.titleLarge) },
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
            Text("AÑADIR NUEVO CONTACTO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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
                label = { Text("Vínculo (Ej. Madre, Hermano)") },
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
                trailingIcon = {
                    IconButton(onClick = {
                        // Dispara el intent a la agenda telefónica del sistema
                        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                        contactLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Importar desde contactos")
                    }
                },
                supportingText = {
                    if (errorTelefono) {
                        Text("Ingrese un teléfono válido")
                    } else {
                        Text("Incluye el código de área/país para asegurar el envío del SMS.")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            DiaDocButton(
                text = if (contactoEditando != null) "GUARDAR CAMBIOS" else "AGREGAR CONTACTO",
                onClick = {
                    viewModel.guardarContacto(uid, nombre, vinculo, telefono, contactoEditando?.codContacto ?: "")
                    if (!errorTelefono) {
                        nombre = ""
                        vinculo = ""
                        telefono = ""
                        contactoEditando = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("MIS CONTACTOS DE EMERGENCIA", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contactos) { contacto ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${contacto.nombreContacto} (${contacto.vinculo})", style = MaterialTheme.typography.titleMedium)
                                Text(contacto.telefono, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row {
                                TextButton(onClick = {
                                    nombre = contacto.nombreContacto
                                    vinculo = contacto.vinculo
                                    telefono = contacto.telefono
                                    contactoEditando = contacto
                                }) { Text("Editar") }

                                TextButton(onClick = { viewModel.eliminarContacto(contacto.codContacto, uid) }) {
                                    Text("Eliminar", color = DiaDocTheme.colors.alertDanger, fontWeight = FontWeight.Bold)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}