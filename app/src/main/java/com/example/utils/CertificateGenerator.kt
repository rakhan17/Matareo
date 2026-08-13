package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.SystemStats
import com.example.R
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

object CertificateGenerator {

    fun generateAndSaveCertificate(
        context: Context,
        stats: SystemStats?,
        cpuScore: Int,
        gpuScore: Int,
        ramScore: Int,
        storageScore: Int
    ): Boolean {
        val totalScore = cpuScore + gpuScore + ramScore + storageScore
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val paint = Paint()
        val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), 
            Color.parseColor("#0B0C10"), Color.parseColor("#1F2833"), Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw Border
        paint.shader = null
        paint.color = Color.parseColor("#45A29E")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        canvas.drawRect(40f, 40f, width - 40f, height - 40f, paint)
        
        // Draw Inner Border
        paint.strokeWidth = 4f
        paint.color = Color.parseColor("#66FCF1")
        canvas.drawRect(60f, 60f, width - 60f, height - 60f, paint)

        paint.style = Paint.Style.FILL

        // Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 80f
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("MATAREO", width / 2f, 200f, paint)
        
        paint.textSize = 40f
        paint.color = Color.parseColor("#45A29E")
        canvas.drawText("OFFICIAL PERFORMANCE CERTIFICATE", width / 2f, 260f, paint)

        // Logo
        try {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            drawable?.let {
                val logoBitmap = it.toBitmap(200, 200)
                canvas.drawBitmap(logoBitmap, (width / 2f) - 100f, 320f, null)
            }
        } catch (e: Exception) { }

        // Device Info
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 35f
        paint.color = Color.LTGRAY
        var yPos = 650f
        val xPos = 120f
        
        canvas.drawText("Device Identity", xPos, yPos, paint)
        yPos += 50f
        paint.color = Color.WHITE
        paint.textSize = 45f
        val deviceName = stats?.deviceName ?: Build.MODEL
        canvas.drawText(deviceName, xPos, yPos, paint)
        
        yPos += 80f
        paint.textSize = 35f
        paint.color = Color.LTGRAY
        canvas.drawText("Hardware Signature", xPos, yPos, paint)
        yPos += 50f
        paint.color = Color.WHITE
        paint.textSize = 45f
        val soc = stats?.hardware ?: Build.HARDWARE
        canvas.drawText(soc, xPos, yPos, paint)

        // Scores Box
        val boxPaint = Paint()
        boxPaint.color = Color.parseColor("#121212")
        boxPaint.alpha = 200
        canvas.drawRoundRect(100f, yPos + 80f, width - 100f, yPos + 600f, 30f, 30f, boxPaint)

        yPos += 160f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 100f
        paint.color = Color.parseColor("#66FCF1")
        canvas.drawText("$totalScore", width / 2f, yPos, paint)
        
        yPos += 60f
        paint.textSize = 40f
        paint.color = Color.LTGRAY
        canvas.drawText("TOTAL MATAREO SCORE", width / 2f, yPos, paint)
        
        yPos += 120f
        paint.textSize = 35f
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        val col1 = 150f
        val col2 = width / 2f + 50f
        
        canvas.drawText("CPU: $cpuScore", col1, yPos, paint)
        canvas.drawText("GPU: $gpuScore", col2, yPos, paint)
        yPos += 80f
        canvas.drawText("RAM: $ramScore", col1, yPos, paint)
        canvas.drawText("Storage: $storageScore", col2, yPos, paint)

        // Footer
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.DKGRAY
        paint.textSize = 25f
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date())
        canvas.drawText("Certified by Matareo Diagnostics Engine", width / 2f, height - 150f, paint)
        canvas.drawText("Generated on: $dateStr", width / 2f, height - 100f, paint)

        return saveBitmapToGallery(context, bitmap, "Matareo_Certificate_${System.currentTimeMillis()}")
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Matareo")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        var uri: Uri? = null
        var outputStream: OutputStream? = null

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            uri = resolver.insert(collection, contentValues)
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
        } finally {
            outputStream?.close()
        }
        return false
    }
}
