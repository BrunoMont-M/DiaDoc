package com.example.diadoc.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.utils.Resource

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authViewModel = AuthViewModel()

        setContent {
            // Estado simple para saber qué pantalla mostrar
            var currentScreen by remember { mutableStateOf("login") }
            val authState by authViewModel.authState.collectAsState()

            // Manejo de éxito (Cuando Firebase responde OK)
            LaunchedEffect(authState) {
                if (authState is Resource.Success) {
                    Toast.makeText(this@LoginActivity, "¡Éxito! Bienvenido", Toast.LENGTH_SHORT).show()
                    // Aquí iría el salto a la pantalla principal más adelante
                }
            }

            if (currentScreen == "login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        authViewModel.resetState()
                        currentScreen = "register"
                    }
                )
            } else {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBackToLogin = {
                        authViewModel.resetState()
                        currentScreen = "login"
                    }
                )
            }
        }
    }
}