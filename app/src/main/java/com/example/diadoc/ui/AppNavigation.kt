package com.example.diadoc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilMedicoViewModel
) {
    val navController = rememberNavController()

    // Verificamos si hay una sesión guardada nativamente en Firebase
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val destinoInicial = if (usuarioActual != null) "verificador_sesion" else "login"

    NavHost(navController = navController, startDestination = destinoInicial) {

        // RUTA 0: Semáforo inteligente (invisible) que decide a dónde mandarte
        composable("verificador_sesion") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            LaunchedEffect(Unit) {
                val existe = perfilViewModel.perfilExiste(uid)
                if (existe) {
                    navController.navigate("dashboard") { popUpTo(0) }
                } else {
                    navController.navigate("perfil_medico/$uid") { popUpTo(0) }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToPerfil = { uid ->
                    navController.navigate("verificador_sesion") { popUpTo(0) }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onNavigateToPerfil = { uid ->
                    navController.navigate("verificador_sesion") { popUpTo(0) }
                }
            )
        }

        composable("perfil_medico/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            PerfilMedicoScreen(
                viewModel = perfilViewModel,
                codUsuarioLogueado = uid,
                onNavigateNext = {
                    navController.navigate("dashboard") { popUpTo(0) }
                }
            )
        }

        composable("dashboard") {
            androidx.compose.material3.Text(text = "¡Dashboard! Tu perfil está guardado y tu sesión activa.")
        }
    }
}