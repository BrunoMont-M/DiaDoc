package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacidadDatosScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidad y Seguridad", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tu información de salud es estrictamente confidencial. En DiaDoc nos tomamos muy en serio la protección de tus datos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PrivacidadItem(
                icon = Icons.Default.Lock,
                title = "Cifrado de extremo a extremo",
                description = "Todas tus métricas biométricas, historial médico y credenciales viajan encriptadas. Ni siquiera nosotros podemos acceder a tus contraseñas."
            )

            PrivacidadItem(
                icon = Icons.Default.Security,
                title = "Base de datos segura",
                description = "Utilizamos infraestructuras de nivel empresarial (Firebase) con reglas de seguridad estrictas. Solo tú puedes leer y escribir tu información de salud."
            )

            PrivacidadItem(
                icon = Icons.Default.Policy,
                title = "No vendemos tus datos",
                description = "Tu perfil médico y las recomendaciones generadas por la IA son para tu uso exclusivo. No compartimos ni vendemos perfiles a terceros o aseguradoras."
            )

            PrivacidadItem(
                icon = Icons.Default.DeleteForever,
                title = "Derecho al olvido",
                description = "Tienes control total sobre tu cuenta. Puedes eliminar permanentemente tu perfil y todo tu historial desde la sección 'Gestión de Cuenta' en cualquier momento."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Para más detalles técnicos o solicitudes sobre tus datos, puedes comunicarte con nuestro equipo de soporte.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrivacidadItem(icon: ImageVector, title: String, description: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}