package com.example.diadoc.ui

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.diadoc.R
import com.example.diadoc.ui.theme.DiaDocTheme
import com.example.diadoc.utils.AlarmReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var timeoutJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configuraciones para encender la pantalla y atravesar el bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val titulo = intent.getStringExtra("TITULO_ALARMA") ?: "Alarma de DiaDoc"
        val debeVibrar = intent.getBooleanExtra("VIBRAR_ALARMA", true)
        val esRepeticion = intent.getBooleanExtra("ES_REPETICION", false)
        val alarmId = intent.getIntExtra("ALARM_ID", System.currentTimeMillis().toInt())
        val tonoUri = intent.getStringExtra("TONO_URI")

        // 2. Iniciar el sonido del despertador y la vibración
        reproducirSonido(tonoUri)
        if (debeVibrar) iniciarVibracionContinua()

        // 3. Temporizador de 1 minuto (60,000 ms)
        timeoutJob = lifecycleScope.launch {
            delay(60_000L) // Esperar 1 minuto
            manejarTiempoAgotado(titulo, debeVibrar, esRepeticion, alarmId, tonoUri) // NUEVO: Pasamos el tono para cuando reprograme
        }

        setContent {
            DiaDocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaDespertador(
                        titulo = titulo,
                        onApagar = {
                            timeoutJob?.cancel()
                            detenerAlarma()
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun manejarTiempoAgotado(titulo: String, vibrar: Boolean, esRepeticion: Boolean, alarmId: Int, tonoUri: String?) {
        detenerAlarma()

        if (!esRepeticion) {
            Toast.makeText(this, "Alarma pospuesta por 5 minutos", Toast.LENGTH_LONG).show()
            programarPosponer(titulo, vibrar, alarmId, tonoUri)
        } else {
            mostrarNotificacionPerdida(titulo, alarmId)
        }
        finish()
    }

    private fun programarPosponer(titulo: String, vibrar: Boolean, alarmId: Int, tonoUri: String?) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TITULO_ALARMA", titulo)
            putExtra("VIBRAR_ALARMA", vibrar)
            putExtra("ES_REPETICION", true)
            putExtra("ALARM_ID", alarmId)
            putExtra("TONO_URI", tonoUri) // NUEVO: Nos aseguramos que suene igual en 5 minutos
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tiempoPosponer = System.currentTimeMillis() + (5 * 60 * 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoPosponer, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoPosponer, pendingIntent)
        }
    }

    private fun mostrarNotificacionPerdida(titulo: String, alarmId: Int) {
        val channelId = "diadoc_alarmas_perdidas"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas Perdidas",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarma perdida")
            .setContentText(titulo)
            .setAutoCancel(true)

        notificationManager.notify(alarmId + 2000, builder.build())
    }

    private fun reproducirSonido(tonoUriString: String?) {
        try {
            // NUEVO: Intentamos usar el tono elegido por el usuario, si no hay, usamos el predeterminado del sistema
            val alarmUri = if (tonoUriString != null) {
                android.net.Uri.parse(tonoUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun iniciarVibracionContinua() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun detenerAlarma() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutJob?.cancel()
        detenerAlarma()
    }
}

@Composable
fun PantallaDespertador(titulo: String, onApagar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Es hora!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = titulo,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onApagar,
            modifier = Modifier
                .size(120.dp)
                .background(DiaDocTheme.colors.alertDanger, CircleShape),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = DiaDocTheme.colors.alertDanger
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("APAGAR", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}