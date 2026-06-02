package com.example.diadoc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.ContactosViewModel
import com.example.diadoc.viewmodel.DashboardViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import com.example.diadoc.viewmodel.GeneradorPlanViewModel
import com.example.diadoc.viewmodel.PlanNutricionalViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilMedicoViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val navController = rememberNavController()
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val destinoInicial = if (usuarioActual != null) "verificador_sesion" else "login"

    NavHost(navController = navController, startDestination = destinoInicial) {

        // RUTA 0: Semáforo inteligente
        composable("verificador_sesion") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            LaunchedEffect(Unit) {
                val existe = perfilViewModel.perfilExiste(uid)
                if (existe) {
                    navController.navigate("dashboard/$uid") { popUpTo(0) }
                } else {
                    navController.navigate("perfil_medico/$uid") { popUpTo(0) }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // RUTA 1: Inicio de Sesión
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToPerfil = {
                    navController.navigate("verificador_sesion") { popUpTo(0) }
                }
            )
        }

        // RUTA 2: Registro de Usuario
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onNavigateToPerfil = {
                    navController.navigate("verificador_sesion") { popUpTo(0) }
                }
            )
        }

        // RUTA 3: Creación/Edición del Perfil Médico
        composable("perfil_medico/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            PerfilMedicoScreen(
                viewModel = perfilViewModel,
                codUsuarioLogueado = uid,
                onNavigateNext = {
                    navController.navigate("dashboard/$uid") { popUpTo(0) }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // RUTA 4: Home
        composable("dashboard/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            DashboardScreen(
                viewModel = dashboardViewModel,
                uid = uid,
                onNavigateToSettings = { navController.navigate("ajustes/$uid") },
                onNavigateToSOS = { /* TODO: Módulo SOS */ },
                onNavigateToGenerador = { navController.navigate("generador_ia/$uid") },
                onNavigateToNutricion = { navController.navigate("plan_nutricional/$uid") }
            )
        }

        // RUTA 5: Contactos de Emergencia
        composable("contactos/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val contactosViewModel: ContactosViewModel = viewModel()

            ContactosScreen(
                viewModel = contactosViewModel,
                uid = uid,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // RUTA 6: Ajustes Generales
        composable("ajustes/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            AjustesScreen(
                uid = uid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToContactos = { navController.navigate("contactos/$uid") },
                onNavigateToPerfil = { navController.navigate("perfil_medico/$uid") }
            )
        }

        // RUTA 7: Generador de IA
        composable("generador_ia/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val generadorViewModel: GeneradorPlanViewModel = viewModel()

            GenerarPlanScreen(
                viewModel = generadorViewModel,
                uid = uid,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // RUTA 8: Plan Nutricional de Hoy
        composable("plan_nutricional/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val planViewModel: PlanNutricionalViewModel = viewModel()

            PlanNutricionalScreen(
                viewModel = planViewModel,
                uid = uid,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}