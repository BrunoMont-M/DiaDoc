package com.example.diadoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.diadoc.ui.AppNavigation
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instanciamos los ViewModels a nivel global para la navegación
        val authViewModel = AuthViewModel()
        val perfilViewModel = PerfilMedicoViewModel()

        setContent {
            DiaDocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        perfilViewModel = perfilViewModel
                    )
                }
            }
        }
    }
}