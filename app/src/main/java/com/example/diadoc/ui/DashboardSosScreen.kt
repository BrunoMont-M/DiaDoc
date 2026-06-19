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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.example.diadoc.viewmodel.BitacoraViewModel
import com.example.diadoc.viewmodel.ContactosViewModel
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun DashboardSosScreen(
    uid: String,
    contactosViewModel: ContactosViewModel = viewModel(),
    bitacoraViewModel: BitacoraViewModel = viewModel()
) {
    val context = LocalContext.current

    // Obtener datos dinámicos
    val listaContactos by contactosViewModel.contactos.collectAsState()
    val historial by bitacoraViewModel.historialReciente.collectAsState()

    // Datos de glucosa
    val registro = historial.firstOrNull { it.descripcion.contains("Glucosa", ignoreCase = true) }
    val valorGlucosa = registro?.descripcion?.replace("Glucosa: ", "") ?: "0"

    // Estados para GPS dinámico
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }

    // Estados para el flujo SOS
    var sosStatus by remember { mutableStateOf("IDLE") }
    var segundos by remember { mutableStateOf(5) }

    // Cargar datos y obtener ubicación al iniciar
    LaunchedEffect(uid) {
        contactosViewModel.cargarContactos(uid)
        bitacoraViewModel.cargarBitacora(uid)

        // Lógica para obtener ubicación real
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

    // Lógica de Envío SMS (Dinámica con GPS real)
    LaunchedEffect(sosStatus) {
        if (sosStatus == "SENT") {
            try {
                val contacto = listaContactos.firstOrNull()
                val numeroDestino = contacto?.telefono

                if (!numeroDestino.isNullOrEmpty()) {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    val nombreUsuario = "Usuario"

                    val mensaje = "$nombreUsuario tiene $valorGlucosa mg/dL de glucosa. Ubicación: $latitud, $longitud"

                    smsManager.sendTextMessage(numeroDestino, null, mensaje, null, null)

                    Toast.makeText(context, "Alerta enviada a $numeroDestino", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: No hay contacto configurado.", Toast.LENGTH_LONG).show()
                    sosStatus = "IDLE"
                }
            } catch (e: Exception) {
                Log.e("DEBUG_SOS", "Error al enviar: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- INTERFAZ ---
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
                        Text("🩸 Glucosa: $valorGlucosa mg/dL", color = Color.White, fontSize = 16.sp)
                        Text("📍 GPS: $latitud, $longitud", color = Color.White, fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { sosStatus = "IDLE"; segundos = 5 }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("REINICIAR")
                        }
                    }
                }
            }
        }
    }
}