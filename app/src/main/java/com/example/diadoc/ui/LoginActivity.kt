package com.example.diadoc.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.Resource
import com.example.diadoc.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authViewModel = AuthViewModel()

        setContent {
            DiaDocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }
                    val authState by authViewModel.authState.collectAsState()

                    LaunchedEffect(authState) {
                        if (authState is Resource.Success) {
                            Toast.makeText(this@LoginActivity, "¡Éxito! Bienvenido", Toast.LENGTH_SHORT).show()
                            // Aquí iría el salto a la pantalla principal
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
    }
}