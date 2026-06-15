package com.example.diadoc.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.diadoc.model.PlanDiario
import com.example.diadoc.model.Usuario
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfManager {

    fun generarYCompartirPDF(
        context: Context,
        usuario: Usuario?,
        planHoy: PlanDiario?,
        metricaDinamica: List<String>,
        racha: Int
    ) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 28f
            paint.color = Color.rgb(33, 150, 243)
            canvas.drawText("Reporte Clínico - DiaDoc", 50f, 80f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 16f
            paint.color = Color.BLACK
            val nombre = usuario?.nomYapeUsuario ?: "Paciente Clínico"
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Paciente: $nombre", 50f, 140f, paint)
            canvas.drawText("Fecha de Emisión: $fecha", 50f, 170f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            canvas.drawText("Estado de Salud Actual", 50f, 240f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 16f
            val tituloMetrica = if (metricaDinamica.size >= 3) metricaDinamica[0] else "Métrica"
            val valorMetrica = if (metricaDinamica.size >= 3) "${metricaDinamica[1]} ${metricaDinamica[2]}" else "N/A"

            canvas.drawText("• $tituloMetrica: $valorMetrica", 50f, 280f, paint)
            canvas.drawText("• Adherencia al tratamiento: $racha días consecutivos", 50f, 310f, paint)
            canvas.drawText("• Hidratación diaria: ${planHoy?.vasosAgua ?: 0} / 8 vasos", 50f, 340f, paint)

            paint.textSize = 12f
            paint.color = Color.GRAY
            canvas.drawText("Documento generado automáticamente por el algoritmo de DiaDoc.", 50f, 780f, paint)

            document.finishPage(page)

            val file = File(context.cacheDir, "Reporte_DiaDoc_${nombre.replace(" ", "_")}.pdf")
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()

            compartirArchivo(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generarReporteProgresoPDF(
        context: Context,
        fechaDesde: String,
        fechaHasta: String,
        promedioGlucosa: Float,
        adherenciaDieta: Float,
        adherenciaEjercicio: Float,
        totalRegistros: Int
    ) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            // Cabecera
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 28f
            paint.color = Color.rgb(211, 47, 47)
            canvas.drawText("Reporte de Progreso Analítico", 50f, 80f, paint)

            // Fechas del Reporte
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 16f
            paint.color = Color.BLACK
            val fechaEmision = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Fecha de Emisión: $fechaEmision", 50f, 130f, paint)
            canvas.drawText("Período Analizado: $fechaDesde al $fechaHasta", 50f, 160f, paint)

            // Resumen Biométrico
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            canvas.drawText("1. Biometría de Glucosa", 50f, 230f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 16f
            canvas.drawText("• Total de controles registrados: $totalRegistros", 50f, 270f, paint)
            canvas.drawText("• Promedio en el período: ${promedioGlucosa.toInt()} mg/dL", 50f, 300f, paint)

            // Adherencia al Tratamiento
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            canvas.drawText("2. Adherencia al Tratamiento", 50f, 370f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 16f
            canvas.drawText("• Cumplimiento Plan Nutricional: ${(adherenciaDieta * 100).toInt()}%", 50f, 410f, paint)
            canvas.drawText("• Cumplimiento Rutina Física: ${(adherenciaEjercicio * 100).toInt()}%", 50f, 440f, paint)

            // Pie de página
            paint.textSize = 12f
            paint.color = Color.GRAY
            canvas.drawText("Documento exportado desde DiaDoc. Compartir únicamente con profesionales de salud.", 50f, 780f, paint)

            document.finishPage(page)

            val file = File(context.cacheDir, "Reporte_Historico_DiaDoc.pdf")
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()

            compartirArchivo(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun compartirArchivo(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Enviar Reporte Médico a..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}