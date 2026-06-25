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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { Text("Gestión de Cuenta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                    Text("Correo Electrónico Vinculado", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(correoActual, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            // Formulario de Cambio de Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Cambiar Contraseña", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
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
                    Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.cambiarPassword(passActual, passNueva) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = passActual.isNotEmpty() && passNueva.length >= 6 && passNueva == passConfirmacion && estadoAccion !is Resource.Loading
                ) {
                    if (estadoAccion is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACTUALIZAR CONTRASEÑA", fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Eliminar Cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zona de Peligro", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Eliminar tu cuenta borrará permanentemente todos tus datos médicos, recetas, historial y acceso. Esta acción no se puede deshacer.", fontSize = 14.sp, color = Color(0xFFB71C1C))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacionEliminar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
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
                title = { Text("¿Eliminar cuenta definitivamente?", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                text = { Text("Perderás todo tu progreso en DiaDoc. Si estás seguro, presiona Continuar para verificar tu identidad.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoConfirmacionEliminar = false
                            mostrarDialogoReauthEliminar = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
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
                title = { Text("Verifica tu Identidad") },
                text = {
                    Column {
                        Text("Por seguridad, ingresa tu contraseña actual para confirmar la eliminación de tu cuenta.")
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
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