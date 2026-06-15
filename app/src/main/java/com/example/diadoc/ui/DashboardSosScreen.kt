package com.example.diadoc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DashboardSosScreen() {
    // Cooldown que arranca en 5 segundos
    var segundosRestantes by remember { mutableStateOf(5) }
    var sosActivo by remember { mutableStateOf(true) }
    var alertaEnviada by remember { mutableStateOf(false) }

    // Paleta de colores Diadoc (Oscuro)
    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val lightRed = Color(0xFFFF4D4D)
    val warningYellow = Color(0xFFFFB300)

    // Temporizador asíncrono para la cuenta regresiva
    LaunchedEffect(sosActivo, segundosRestantes) {
        if (sosActivo && segundosRestantes > 0) {
            delay(1000L)
            segundosRestantes -= 1
            if (segundosRestantes == 0) {
                alertaEnviada = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // --- FONDO: CONTENIDO DEL DASHBOARD PRINCIPAL (CORREGIDO Y CENTRADO) ---
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally // 🚨 Centra los elementos de fondo horizontalmente
        ) {
            Text(
                text = "🏠 Dashboard Principal",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp)
            )

            // Espacio libre donde se superpone la tarjeta S.O.S.
            Spacer(modifier = Modifier.height(220.dp))

            // Sección inferior: Última Glucemia Centrada
            Text(
                text = "TU ÚLTIMA GLUCEMIA",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(0.9f), // Le da un ancho contenido y estético
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally // 🚨 Centra los textos dentro de la tarjeta de glucemia
                ) {
                    Text(
                        text = "65 mg/dL",
                        color = lightRed,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "(Hipoglucemia detectada)",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // --- FRENTE: TARJETA SUPERPUESTA DE ALERTA S.O.S. ---
        AnimatedVisibility(
            visible = sosActivo,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 2.dp, color = if (alertaEnviada) lightRed else warningYellow, shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Título dinámico centrado
                    Text(
                        text = if (alertaEnviada) "🚨 ALERTA S.O.S. ENVIADA" else "🚨 ALERTA S.O.S. EN CURSO...",
                        color = lightRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Texto descriptivo centrado
                    Text(
                        text = if (alertaEnviada)
                            "Ubicación reportada a tus contactos de emergencia."
                        else
                            "Enviando ubicación a tus contactos en:",
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Contador de segundos centrado
                    if (!alertaEnviada) {
                        Text(
                            text = "[  $segundosRestantes  ]",
                            color = warningYellow,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "(Segundos restantes)",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Geolocalización fija (Mendoza) centrada
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(text = "📍 ", fontSize = 16.sp)
                        Text(
                            text = "Capturando GPS: 32.889° S, 68.845° W",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón para cancelar la falsa alarma centrado
                    Button(
                        onClick = { sosActivo = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = lightRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CANCELAR (Falsa Alarma)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}