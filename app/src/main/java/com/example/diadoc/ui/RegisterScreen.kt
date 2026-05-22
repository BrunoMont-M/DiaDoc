package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.utils.Resource

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onBackToLogin: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre y Apellido") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

        Spacer(modifier = Modifier.height(32.dp))

        if (state is Resource.Loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.register(email, password, nombre) }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrarme")
            }
        }

        if (state is Resource.Error) {
            Text((state as Resource.Error).message, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = onBackToLogin) {
            Text("¿Ya tenés cuenta? Iniciá sesión")
        }
    }
}