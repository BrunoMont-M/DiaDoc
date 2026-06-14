package com.example.diadoc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.diadoc.viewmodel.AuthViewModel
import com.example.diadoc.viewmodel.ContactosViewModel
import com.example.diadoc.viewmodel.DashboardViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import com.example.diadoc.viewmodel.GeneradorPlanViewModel
import com.example.diadoc.viewmodel.PlanNutricionalViewModel
// FASE US11: Agregamos el import del nuevo ViewModel
import com.example.diadoc.viewmodel.BitacoraViewModel
import com.google.firebase.auth.FirebaseAuth
// FASE US14: Agregamos el import de tu nueva pantalla y su ViewModel
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel
import com.example.diadoc.ui.CatalogoAlimentosScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilMedicoViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val navController = rememberNavController()
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val destinoInicial = if (usuarioActual != null) "verificador_sesion" else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null &&
            !currentRoute.startsWith("login") &&
            !currentRoute.startsWith("register") &&
            !currentRoute.startsWith("verificador_sesion")

    val uidGlobal = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = currentRoute?.startsWith("dashboard") == true,
                        onClick = { navController.navigate("dashboard/$uidGlobal") { popUpTo("dashboard/$uidGlobal") { inclusive = false } } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Nutrición") },
                        label = { Text("Nutrición") },
                        selected = currentRoute?.startsWith("plan_nutricional") == true,
                        onClick = { navController.navigate("plan_nutricional/$uidGlobal") { popUpTo("dashboard/$uidGlobal") { inclusive = false } } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Actividad") },
                        label = { Text("Actividad") },
                        selected = currentRoute?.startsWith("actividad") == true,
                        onClick = { /* TODO: Pantalla de Actividad */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Progreso") },
                        label = { Text("Progreso") },
                        selected = currentRoute?.startsWith("progreso") == true,
                        onClick = { /* TODO: Pantalla de Progress */ }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(paddingValues)
        ) {
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

            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToPerfil = { navController.navigate("verificador_sesion") { popUpTo(0) } }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() },
                    onNavigateToPerfil = { navController.navigate("verificador_sesion") { popUpTo(0) } }
                )
            }

            composable("perfil_medico/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                PerfilMedicoScreen(
                    viewModel = perfilViewModel,
                    codUsuarioLogueado = uid,
                    onNavigateNext = { navController.navigate("dashboard/$uid") { popUpTo(0) } },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("dashboard/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    uid = uid,
                    onNavigateToSettings = { navController.navigate("ajustes/$uid") },
                    onNavigateToSOS = { /* TODO: Módulo SOS */ },
                    onNavigateToGenerador = { navController.navigate("generador_ia/$uid") },
                    onNavigateToBitacora = { navController.navigate("bitacora/$uid") },
                    onNavigateToCatalogo = { navController.navigate("catalogo_alimentos") } // Enlazamos tu pantalla al botón del Dashboard
                )
            }

            composable("contactos/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val contactosViewModel: ContactosViewModel = viewModel()
                ContactosScreen(
                    viewModel = contactosViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("ajustes/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                AjustesScreen(
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToContactos = { navController.navigate("contactos/$uid") },
                    onNavigateToPerfil = { navController.navigate("perfil_medico/$uid") }
                )
            }

            composable("generador_ia/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val generadorViewModel: GeneradorPlanViewModel = viewModel()
                GenerarPlanScreen(
                    viewModel = generadorViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("plan_nutricional/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val planViewModel: PlanNutricionalViewModel = viewModel()
                PlanNutricionalScreen(
                    viewModel = planViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("bitacora/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val bitacoraViewModel: BitacoraViewModel = viewModel()
                BitacoraScreen(
                    viewModel = bitacoraViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("catalogo_alimentos") {
                val catalogoViewModel: CatalogoAlimentosViewModel = viewModel()
                CatalogoAlimentosScreen(
                    viewModel = catalogoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}