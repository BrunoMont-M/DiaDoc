package com.example.diadoc.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.diadoc.repository.PreferenciasRepository
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.DashboardViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciamos el repositorio único
        val preferenciasRepository = PreferenciasRepository(applicationContext)

        // 2. Instanciamos los ViewModels inyectando el repositorio
        val authViewModel = AuthViewModel()
        val perfilViewModel = PerfilMedicoViewModel(preferenciasRepository = preferenciasRepository)
        val dashboardViewModel = DashboardViewModel(preferenciasRepository = preferenciasRepository)
        val catalogoViewModel = CatalogoAlimentosViewModel()

        setContent {
            // AHORA SÍ: Escuchamos la base de datos en tiempo real desde la raíz
            val temaApp by preferenciasRepository.temaAppFlow.collectAsState(initial = 0)
            val paletaApp by preferenciasRepository.paletaAppFlow.collectAsState(initial = 0)

            val darkTheme = when (temaApp) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            // Inyectamos el estado dinámico al Tema
            DiaDocTheme(darkTheme = darkTheme, paleta = paletaApp, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        perfilViewModel = perfilViewModel,
                        dashboardViewModel = dashboardViewModel,
                        catalogoViewModel = catalogoViewModel
                    )
                }
            }
        }
    }
}