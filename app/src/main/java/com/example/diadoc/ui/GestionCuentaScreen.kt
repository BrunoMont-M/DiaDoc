package com.example.diadoc.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.GestionCuentaViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCuentaScreen(
    viewModel: GestionCuentaViewModel,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val estadoAccion by viewModel.estadoAccion.collectAsState()
    val correoActual = FirebaseAuth.getInstance().currentUser?.email ?: "Correo no disponible"

    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passConfirmacion by remember { mutableStateOf("") }

    var passActualVisible by remember { mutableStateOf(false) }
    var passNuevaVisible by remember { mutableStateOf(false) }

    var mostrarDialogoConfirmacionEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoReauthEliminar by remember { mutableStateOf(false) }
    var passParaEliminar by remember { mutableStateOf("") }

    val alertDanger = DiaDocTheme.colors.alertDanger
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(estadoAccion) {
        when (estadoAccion) {
            is Resource.Success -> {
                val accion = (estadoAccion as Resource.Success).data
                if (accion == "CONTRASEÑA_CAMBIADA") {
                    Toast.makeText(context, "Contraseña actualizada con éxito.", Toast.LENGTH_LONG).show()
                    passActual = ""; passNueva = ""; passConfirmacion = ""
                    viewModel.limpiarEstado()
                } else if (accion == "CUENTA_ELIMINADA") {
                    Toast.makeText(context, "Cuenta eliminada permanentemente.", Toast.LENGTH_LONG).show()
                    viewModel.limpiarEstado()
                    onAccountDeleted()
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, (estadoAccion as Resource.Error).message, Toast.LENGTH_LONG).show()
                viewModel.limpiarEstado()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta de Información
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Correo Electrónico Vinculado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(correoActual, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            // Formulario de Cambio de Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Cambiar Contraseña", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passActual,
                    onValueChange = { passActual = it },
                    label = { Text("Contraseña Actual") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passActualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passActualVisible = !passActualVisible }) {
                            Icon(if (passActualVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passNueva,
                    onValueChange = { passNueva = it },
                    label = { Text("Nueva Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) {
                            Icon(if (passNuevaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passConfirmacion,
                    onValueChange = { passConfirmacion = it },
                    label = { Text("Confirmar Nueva Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passNueva.isNotEmpty() && passConfirmacion.isNotEmpty() && passNueva != passConfirmacion
                )

                if (passNueva.isNotEmpty() && passConfirmacion.isNotEmpty() && passNueva != passConfirmacion) {
                    Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                DiaDocButton(
                    text = "ACTUALIZAR CONTRASEÑA",
                    icon = rememberVectorPainter(Icons.Default.Lock),
                    onClick = { viewModel.cambiarPassword(passActual, passNueva) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = passActual.isNotEmpty() && passNueva.length >= 6 && passNueva == passConfirmacion && estadoAccion !is Resource.Loading
                )

                if (estadoAccion is Resource.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Eliminar Cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(alertDanger.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = alertDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zona de Peligro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = alertDanger)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Eliminar tu cuenta borrará permanentemente todos tus datos médicos, recetas, historial y acceso. Esta acción no se puede deshacer.", style = MaterialTheme.typography.bodyMedium, color = alertDanger)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacionEliminar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = alertDanger)
                ) {
                    Text("ELIMINAR MI CUENTA", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Modal 1: ¿Estás seguro?
        if (mostrarDialogoConfirmacionEliminar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoConfirmacionEliminar = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("¿Eliminar cuenta definitivamente?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = alertDanger) },
                text = { Text("Perderás todo tu progreso en DiaDoc. Si estás seguro, presiona Continuar para verificar tu identidad.", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoConfirmacionEliminar = false
                            mostrarDialogoReauthEliminar = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = alertDanger)
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoConfirmacionEliminar = false }) { Text("Cancelar") }
                }
            )
        }

        // Modal 2: Reautenticación estricta
        if (mostrarDialogoReauthEliminar) {
            AlertDialog(
                onDismissRequest = {
                    mostrarDialogoReauthEliminar = false
                    passParaEliminar = ""
                },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Verifica tu Identidad", style = MaterialTheme.typography.titleMedium) },
                text = {
                    Column {
                        Text("Por seguridad, ingresa tu contraseña actual para confirmar la eliminación de tu cuenta.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = passParaEliminar,
                            onValueChange = { passParaEliminar = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoReauthEliminar = false
                            viewModel.eliminarCuenta(passParaEliminar)
                            passParaEliminar = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = alertDanger),
                        enabled = passParaEliminar.isNotEmpty() && estadoAccion !is Resource.Loading
                    ) {
                        Text("Eliminar Definitivamente")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mostrarDialogoReauthEliminar = false
                        passParaEliminar = ""
                    }) { Text("Cancelar") }
                }
            )
        }
    }
}