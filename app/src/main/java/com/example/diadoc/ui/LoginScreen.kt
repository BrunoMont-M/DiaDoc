package com.example.diadoc.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToPerfil: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginState by viewModel.authState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        if (loginState is Resource.Success<*>) {
            val uid = (loginState as Resource.Success<String>).data
            viewModel.resetState()
            onNavigateToPerfil(uid ?: "")
        }
    }

    LaunchedEffect(resetPasswordState) {
        if (resetPasswordState is Resource.Success<*>) {
            val mensaje = (resetPasswordState as Resource.Success<String>).data
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            viewModel.clearResetPasswordState()
        } else if (resetPasswordState is Resource.Error) {
            val errorMsg = (resetPasswordState as Resource.Error).message ?: "Error desconocido"
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearResetPasswordState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text(text = "DiaDoc", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Iniciar Sesión", style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp), color = MaterialTheme.colorScheme.onBackground)
            Text(text = "Tu salud bajo control, siempre", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, "Contraseña") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ver contraseña")
                }
            }
        )

        TextButton(onClick = { viewModel.recuperarPassword(email) }, modifier = Modifier.align(Alignment.End)) {
            Text(text = "¿Olvidaste tu contraseña?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Tus datos clínicos están encriptados bajo normas de privacidad.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
        Spacer(modifier = Modifier.height(24.dp))

        if (loginState is Resource.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp)
            ) { Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold) }
        }

        if (loginState is Resource.Error) {
            Text(text = (loginState as Resource.Error).message ?: "Error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tenés una cuenta? Registrate", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
        }
    }
}