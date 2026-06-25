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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.example.diadoc.viewmodel.BitacoraViewModel
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel
import com.example.diadoc.viewmodel.GeneradorRutinaViewModel
import com.example.diadoc.viewmodel.ReporteProgresoViewModel
import com.example.diadoc.viewmodel.RecetarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilMedicoViewModel,
    dashboardViewModel: DashboardViewModel,
    catalogoViewModel: CatalogoAlimentosViewModel
) {
    val navController = rememberNavController()
    val usuarioActual = FirebaseAuth.getInstance().currentUser

    val destinoInicial = if (usuarioActual != null) "verificador_inicio" else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null &&
            !currentRoute.startsWith("login") &&
            !currentRoute.startsWith("register") &&
            !currentRoute.startsWith("verificador_")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = currentRoute?.startsWith("dashboard") == true,
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            navController.navigate("dashboard/$uid") { popUpTo("dashboard/$uid") { inclusive = false } }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Nutrición") },
                        label = { Text("Nutrición") },
                        selected = currentRoute == "menu_nutricion" || currentRoute == "registrar_alimento" || currentRoute == "crear_receta",
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            navController.navigate("menu_nutricion") { popUpTo("dashboard/$uid") { inclusive = false } }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Actividad") },
                        label = { Text("Actividad") },
                        selected = currentRoute?.startsWith("actividad") == true,
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            navController.navigate("actividad/$uid") { popUpTo("dashboard/$uid") { inclusive = false } }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Progreso") },
                        label = { Text("Progreso") },
                        selected = currentRoute?.startsWith("progreso") == true,
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            navController.navigate("progreso/$uid") { popUpTo("dashboard/$uid") { inclusive = false } }
                        }
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

            composable("verificador_inicio") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                LaunchedEffect(Unit) {
                    if (uid.isNotEmpty()) {
                        val codRol = perfilViewModel.obtenerCodRol(uid)
                        if (codRol == 2) {
                            navController.navigate("dashboard/$uid") { popUpTo("verificador_inicio") { inclusive = true } }
                        } else {
                            val existe = perfilViewModel.perfilExiste(uid)
                            if (existe) {
                                navController.navigate("dashboard/$uid") { popUpTo("verificador_inicio") { inclusive = true } }
                            } else {
                                navController.navigate("perfil_medico/$uid") { popUpTo("verificador_inicio") { inclusive = true } }
                            }
                        }
                    } else {
                        navController.navigate("login") { popUpTo("verificador_inicio") { inclusive = true } }
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            composable("verificador_login/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                LaunchedEffect(Unit) {
                    if (uid.isNotEmpty()) {
                        delay(600)

                        val codRol = perfilViewModel.obtenerCodRol(uid)
                        if (codRol == 2) {
                            navController.navigate("dashboard/$uid") { popUpTo("login") { inclusive = true } }
                        } else {
                            val existe = perfilViewModel.perfilExiste(uid)
                            if (existe) {
                                navController.navigate("dashboard/$uid") { popUpTo("login") { inclusive = true } }
                            } else {
                                navController.navigate("perfil_medico/$uid") { popUpTo("login") { inclusive = true } }
                            }
                        }
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
                    onNavigateToPerfil = { uidDevuelto ->
                        navController.navigate("verificador_login/$uidDevuelto") { popUpTo(0) }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() },
                    onNavigateToPerfil = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        navController.navigate("verificador_login/$uid") { popUpTo(0) }
                    }
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
                val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: uid
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    uid = userUid,
                    onNavigateToSettings = { navController.navigate("ajustes/$uid") },
                    onNavigateToSOS = { navController.navigate("dashboard_sos") },
                    onNavigateToGenerador = { navController.navigate("generador_ia/$uid") },
                    onNavigateToBitacora = { navController.navigate("bitacora/$uid") },
                    onNavigateToActividad = { navController.navigate("actividad/$uid") }
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
                val usuarioLogueado by dashboardViewModel.usuario.collectAsState()
                val esUsuarioAdmin = usuarioLogueado?.codRol == 2

                AjustesScreen(
                    uid = uid,
                    isAdmin = esUsuarioAdmin,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToContactos = { navController.navigate("contactos/$uid") },
                    onNavigateToPerfil = { navController.navigate("perfil_medico/$uid") },
                    onNavigateToCatalogoAlimentos = { navController.navigate("catalogo_alimentos") },
                    onNavigateToCatalogoEjercicios = { navController.navigate("catalogo_ejercicios") },
                    onNavigateToGestionCuenta = { navController.navigate("gestion_cuenta/$uid") },
                    onLogOut = {
                        dashboardViewModel.limpiarDatos()
                        perfilViewModel.limpiarDatos()

                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }

            composable("gestion_cuenta/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val gestionCuentaViewModel: com.example.diadoc.viewmodel.GestionCuentaViewModel = viewModel()

                GestionCuentaScreen(
                    viewModel = gestionCuentaViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAccountDeleted = {
                        dashboardViewModel.limpiarDatos()
                        perfilViewModel.limpiarDatos()

                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
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

            composable("actividad/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val generadorRutinaViewModel: GeneradorRutinaViewModel = viewModel()
                GeneradorRutinaScreen(
                    viewModel = generadorRutinaViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditor = { navController.navigate("editor_rutina/$uid") }
                )
            }

            composable("editor_rutina/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val generadorRutinaViewModel: GeneradorRutinaViewModel = viewModel()
                EditorRutinaScreen(
                    viewModel = generadorRutinaViewModel,
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
                CatalogoAlimentosScreen(
                    viewModel = catalogoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("catalogo_ejercicios") {
                val ejerciciosViewModel: com.example.diadoc.viewmodel.CatalogoEjerciciosViewModel = viewModel()
                CatalogoEjerciciosScreen(
                    viewModel = ejerciciosViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("dashboard_sos") {
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                DashboardSosScreen(uid = currentUserUid)
            }

            composable("menu_nutricion") {
                NutricionMenuScreen(
                    onNavigateToPlanNutricional = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        navController.navigate("plan_nutricional/$uid")
                    },
                    onNavigateToRegistrarAlimento = { navController.navigate("registrar_alimento") },
                    onNavigateToCrearReceta = { navController.navigate("crear_receta") },
                    onNavigateToRecetario = { navController.navigate("recetario") }
                )
            }

            composable("recetario") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val recetarioViewModel: RecetarioViewModel = viewModel() // ViewModel para listar

                RecetarioScreen(
                    uid = uid,
                    viewModel = recetarioViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("crear_receta") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val recetarioViewModel: RecetarioViewModel = viewModel()

                CrearRecetaScreen(
                    uid = uid,
                    recetarioViewModel = recetarioViewModel,
                    catalogoViewModel = catalogoViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("registrar_alimento") {
                RegistrarAlimentoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("progreso/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val reporteProgresoViewModel: ReporteProgresoViewModel = viewModel()
                ReporteProgresoScreen(
                    viewModel = reporteProgresoViewModel,
                    uid = uid,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}