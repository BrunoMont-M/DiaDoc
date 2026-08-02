package com.example.diadoc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.example.diadoc.ui.components.DiaDocButton
import com.example.diadoc.ui.theme.DiaDocTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    uid: String,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToContactos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToCatalogoAlimentos: () -> Unit,
    onNavigateToCatalogoEjercicios: () -> Unit,
    onNavigateToGestionCuenta: () -> Unit,
    onNavigateToUnidadesMedida: () -> Unit,
    onNavigateToPrivacidad: () -> Unit,
    onNavigateToPersonalizacion: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onLogOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes Generales", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Cuenta y seguridad
            CategoriaAjustes(titulo = "Cuenta y Seguridad")
            ItemAjuste(
                icono = Icons.Default.Person,
                titulo = "Gestión de Cuenta",
                subtitulo = "Contraseña, correo y sesión",
                onClick = onNavigateToGestionCuenta
            )
            ItemAjuste(
                icono = Icons.Default.Lock,
                titulo = "Privacidad de Datos",
                subtitulo = "Encriptación y manejo de datos médicos",
                onClick = onNavigateToPrivacidad
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Salud y tratamiento
            CategoriaAjustes(titulo = "Salud y Tratamiento")
            ItemAjuste(
                icono = Icons.Default.MedicalInformation,
                titulo = "Mi Perfil Médico",
                subtitulo = "Peso, altura, patologías y alergias",
                onClick = onNavigateToPerfil
            )
            ItemAjuste(
                icono = Icons.Default.ContactEmergency,
                titulo = "Red de Contención",
                subtitulo = "Contactos de emergencia y S.O.S.",
                onClick = onNavigateToContactos
            )
            ItemAjuste(
                icono = Icons.Default.Scale,
                titulo = "Unidades de Medida",
                subtitulo = "mg/dL, kg, cm",
                onClick = onNavigateToUnidadesMedida
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Preferencias de la App
            CategoriaAjustes(titulo = "Preferencias de la Aplicación")
            ItemAjuste(
                icono = Icons.Default.Palette,
                titulo = "Personalización",
                subtitulo = "Tema claro/oscuro y paleta de colores",
                onClick = onNavigateToPersonalizacion
            )
            ItemAjuste(
                icono = Icons.Default.Notifications,
                titulo = "Notificaciones y Alertas",
                subtitulo = "Recordatorios de rutinas y comidas",
                onClick = onNavigateToNotificaciones
            )

            if (isAdmin) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CategoriaAjustes(titulo = "Opciones de Administrador")
                ItemAjuste(
                    icono = Icons.Default.RestaurantMenu,
                    titulo = "Catálogo de Alimentos",
                    subtitulo = "Gestión de BD (Admin)",
                    onClick = onNavigateToCatalogoAlimentos
                )
                ItemAjuste(
                    icono = Icons.Default.FitnessCenter,
                    titulo = "Catálogo de Ejercicios",
                    subtitulo = "Gestión de BD (Admin)",
                    onClick = onNavigateToCatalogoEjercicios
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Acerca de
            CategoriaAjustes(titulo = "Acerca de")
            ItemAjuste(
                icono = Icons.Default.SupportAgent,
                titulo = "Soporte Técnico",
                subtitulo = "Ayuda y contacto",
                onClick = { /* TODO: Acción de contacto */ }
            )
            ItemAjuste(
                icono = Icons.Default.Info,
                titulo = "Versión de la Aplicación",
                subtitulo = "DiaDoc v1.0.0 (Build 14)",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DiaDocButton(
                text = "CERRAR SESIÓN",
                onClick = onLogOut,
                containerColor = DiaDocTheme.colors.alertDanger,
                icon = rememberVectorPainter(Icons.Default.Logout),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoriaAjustes(titulo: String) {
    Text(
        text = titulo.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun ItemAjuste(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = titulo,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ir",
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}