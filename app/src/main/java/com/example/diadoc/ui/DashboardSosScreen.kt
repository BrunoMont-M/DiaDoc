package com.example.diadoc.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.example.diadoc.viewmodel.BitacoraViewModel
import com.example.diadoc.viewmodel.ContactosViewModel
import com.example.diadoc.viewmodel.PerfilMedicoViewModel
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun DashboardSosScreen(
    uid: String,
    contactosViewModel: ContactosViewModel = viewModel(),
    bitacoraViewModel: BitacoraViewModel = viewModel(),
    perfilViewModel: PerfilMedicoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Obtener datos en tiempo real de Firebase
    val listaContactos by contactosViewModel.contactos.collectAsState()
    val historial by bitacoraViewModel.historialReciente.collectAsState()

    val usuario by perfilViewModel.usuario.collectAsState()
    val patologiasCatalogo by perfilViewModel.patologias.collectAsState()
    val patologiasDelUsuarioIDs by perfilViewModel.patologiasPrevias.collectAsState()

    // 1. Datos Personales Dinámicos
    val nombreReal = usuario?.nomYapeUsuario ?: "Un familiar"

    // 2. Procesamiento Reactivo Puro
    val textoPatologias by remember(patologiasCatalogo, patologiasDelUsuarioIDs) {
        derivedStateOf {
            val nombres = patologiasCatalogo
                .filter { patologia ->
                    patologiasDelUsuarioIDs.any { id -> id.trim() == patologia.codPatologia.trim() }
                }
                .map { it.nombreEnfermedad }

            if (nombres.isNotEmpty()) nombres.joinToString(", ") else "una condición médica"
        }
    }

    // 3. Detección Inteligente del Último Signo Vital
    val ultimoRegistro = historial.firstOrNull()
    val datoCritico = if (ultimoRegistro != null) {
        "Mi último control indica: ${ultimoRegistro.descripcion}."
    } else {
        "No tengo controles vitales registrados recientemente."
    }

    // Estados para GPS dinámico
    var latitud by remember { mutableDoubleStateOf(0.0) }
    var longitud by remember { mutableDoubleStateOf(0.0) }

    // Estados para la interfaz
    var sosStatus by remember { mutableStateOf("IDLE") }
    var segundos by remember { mutableStateOf(5) }

    // Al abrir la pantalla, cargamos todo el ecosistema
    LaunchedEffect(uid) {
        contactosViewModel.cargarContactos(uid)
        bitacoraViewModel.cargarBitacora(uid)
        perfilViewModel.cargarDatosIniciales(uid)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latitud = it.latitude
                    longitud = it.longitude
                }
            }
        }
    }

    // Lógica robusta de envío múltiple
    LaunchedEffect(sosStatus) {
        if (sosStatus == "SENT") {
            try {
                if (listaContactos.isNotEmpty()) {
                    val smsManager = context.getSystemService(SmsManager::class.java)

                    val linkMaps = "https://maps.google.com/?q=$latitud,$longitud"

                    // Mensaje completo, detallado y sin emojis para garantizar la mejor codificación posible
                    val mensajeCompleto = "S.O.S. EMERGENCIA\n" +
                            "Soy $nombreReal.\n" +
                            "Soy paciente con $textoPatologias y necesito asistencia urgente.\n" +
                            "$datoCritico\n" +
                            "Mi ubicación exacta:\n" +
                            linkMaps

                    val partesMensaje = smsManager.divideMessage(mensajeCompleto)

                    var enviados = 0

                    listaContactos.forEach { contacto ->
                        val numeroDestino = contacto.telefono
                        if (numeroDestino.isNotBlank()) {
                            smsManager.sendMultipartTextMessage(numeroDestino, null, partesMensaje, null, null)
                            enviados++

                            // Delay de 3 segundos para un envío seguro y constante
                            delay(3000)
                        }
                    }

                    Toast.makeText(context, "Alerta enviada a $enviados contactos.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: No hay contactos configurados.", Toast.LENGTH_LONG).show()
                    sosStatus = "IDLE"
                    segundos = 5
                }
            } catch (e: Exception) {
                Log.e("DEBUG_SOS", "Error al enviar: ${e.message}")
                Toast.makeText(context, "Error al enviar SMS. Revisa los permisos.", Toast.LENGTH_LONG).show()
                sosStatus = "IDLE"
                segundos = 5
            }
        }
    }

    // --- INTERFAZ VISUAL ---
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121214)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().border(2.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("🚨 ALERTA S.O.S.", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                when (sosStatus) {
                    "IDLE" -> {
                        Button(onClick = { sosStatus = "COUNTDOWN" }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("ACTIVAR SOS", fontWeight = FontWeight.Bold)
                        }
                    }
                    "COUNTDOWN" -> {
                        Text("ENVIANDO EN...", color = Color.White)
                        Text("$segundos", color = Color.Red, fontSize = 60.sp, fontWeight = FontWeight.ExtraBold)

                        LaunchedEffect(Unit) {
                            repeat(5) {
                                delay(1000)
                                segundos--
                            }
                            sosStatus = "SENT"
                        }
                    }
                    "SENT" -> {
                        Text("¡ALERTA ENVIADA!", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Se notificó a toda tu red de contención.", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text("📍 Ubicación y estado enviados.", color = Color.LightGray, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { sosStatus = "IDLE"; segundos = 5 }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("REINICIAR ALERTA")
                        }
                    }
                }
            }
        }
    }
}