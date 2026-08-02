package com.example.diadoc

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
import com.example.diadoc.ui.AppNavigation
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.DashboardViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenciasRepository = PreferenciasRepository(applicationContext)

        val authViewModel = AuthViewModel()
        val perfilViewModel = PerfilMedicoViewModel(preferenciasRepository = preferenciasRepository)
        val dashboardViewModel = DashboardViewModel(preferenciasRepository = preferenciasRepository)
        val catalogoViewModel = CatalogoAlimentosViewModel()

        setContent {
            val temaApp by preferenciasRepository.temaAppFlow.collectAsState(initial = 0)
            val paletaApp by preferenciasRepository.paletaAppFlow.collectAsState(initial = 0)

            val darkTheme = when (temaApp) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            // dynamicColor = false evita que Android sobrescriba nuestro color
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