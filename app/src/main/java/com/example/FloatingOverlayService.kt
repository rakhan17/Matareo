package com.example

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

class FloatingOverlayService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: LinearLayout
    private lateinit var fpsText: TextView
    private lateinit var cpuText: TextView
    private lateinit var ramText: TextView
    private lateinit var netText: TextView
    private lateinit var pingText: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private lateinit var activityManager: ActivityManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        floatingView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val backgroundDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#CC121212")) // Dark semi-transparent
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#44FFFFFF"))
            }
            background = backgroundDrawable
            setPadding(32, 32, 32, 32)
            
            val title = TextView(this@FloatingOverlayService).apply {
                text = "MATAREO HUD"
                setTextColor(Color.parseColor("#88FFFFFF"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, 16)
                gravity = Gravity.CENTER
            }
            
            fpsText = createMetricView("FPS", "#00FF00")
            cpuText = createMetricView("CPU", "#FF5555")
            ramText = createMetricView("RAM", "#55AAFF")
            netText = createMetricView("NET", "#FFDD55")
            pingText = createMetricView("PING", "#FFAA00")
            
            addView(title)
            addView(fpsText)
            addView(cpuText)
            addView(ramText)
            addView(netText)
            addView(pingText)
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
        
        isRunning = true
        startUpdatingMetrics()
    }
    
    private fun createMetricView(label: String, colorHex: String): TextView {
        return TextView(this).apply {
            text = "$label: --"
            setTextColor(Color.parseColor(colorHex))
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setPadding(0, 4, 0, 4)
        }
    }
    
    private fun startUpdatingMetrics() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                
                // FPS Simulation (Since real FPS needs frame callbacks on target app)
                val fps = Random.nextInt(58, 61)
                fpsText.text = "FPS: $fps"
                
                // CPU Simulation
                val cpu = Random.nextInt(15, 45)
                cpuText.text = "CPU: $cpu%"
                
                // Real RAM 
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val totalGb = memoryInfo.totalMem / (1024 * 1024 * 1024.0)
                val availGb = memoryInfo.availMem / (1024 * 1024 * 1024.0)
                val usedGb = totalGb - availGb
                ramText.text = String.format("RAM: %.1f / %.1f GB", usedGb, totalGb)
                
                // Network Simulation (kbps)
                val net = Random.nextInt(10, 1500)
                if (net > 1000) {
                    netText.text = String.format("NET: %.1f MB/s", net / 1000.0)
                } else {
                    netText.text = "NET: $net KB/s"
                }
                
                // Real Ping
                serviceScope.launch(Dispatchers.IO) {
                    val pingLatency = measurePing("8.8.8.8")
                    withContext(Dispatchers.Main) {
                        if (pingLatency >= 0) {
                            pingText.text = "PING: ${pingLatency}ms"
                            pingText.setTextColor(if (pingLatency < 80) Color.GREEN else if (pingLatency < 150) Color.YELLOW else Color.RED)
                        } else {
                            pingText.text = "PING: Timeout"
                            pingText.setTextColor(Color.RED)
                        }
                    }
                }
                
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun measurePing(ip: String): Int {
        try {
            val process = Runtime.getRuntime().exec("ping -c 1 -W 1 $ip")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("time=")) {
                    val timeString = line!!.substringAfter("time=").substringBefore(" ms")
                    return timeString.toFloatOrNull()?.toInt() ?: -1
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning = false
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
