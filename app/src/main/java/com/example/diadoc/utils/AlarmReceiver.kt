package com.example.diadoc.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.diadoc.R
import com.example.diadoc.ui.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("TITULO_ALARMA") ?: "Recordatorio de DiaDoc"
        val vibrar = intent.getBooleanExtra("VIBRAR_ALARMA", true)
        val esRepeticion = intent.getBooleanExtra("ES_REPETICION", false)
        val alarmId = intent.getIntExtra("ALARM_ID", System.currentTimeMillis().toInt())
        val tonoUri = intent.getStringExtra("TONO_URI")

        mostrarNotificacionDespertador(context, titulo, vibrar, esRepeticion, alarmId, tonoUri)
    }

    private fun mostrarNotificacionDespertador(
        context: Context,
        titulo: String,
        vibrar: Boolean,
        esRepeticion: Boolean,
        alarmId: Int,
        tonoUri: String?
    ) {
        val channelId = "diadoc_alarmas_despertador"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas Completas (Despertador)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para alarmas que encienden la pantalla"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // INTENT A LA PANTALLA DE ALARMA
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("TITULO_ALARMA", titulo)
            putExtra("VIBRAR_ALARMA", vibrar)
            putExtra("ES_REPETICION", esRepeticion)
            putExtra("ALARM_ID", alarmId)
            putExtra("TONO_URI", tonoUri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Alarma DiaDoc!")
            .setContentText(titulo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        notificationManager.notify(alarmId, builder.build())
    }
}