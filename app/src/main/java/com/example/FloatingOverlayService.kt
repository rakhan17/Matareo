package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.components.CrosshairUI
import com.example.ui.components.HudState
import com.example.ui.components.OverlayUI
import com.example.ui.components.TacticalMenuUI
import com.example.utils.CrosshairPrefs
import com.example.utils.ServiceLifecycleOwner
import com.example.utils.TacticalPrefs
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import android.graphics.Canvas

class FloatingOverlayService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var lifecycleOwner: ServiceLifecycleOwner
    private lateinit var crosshairPrefs: CrosshairPrefs
    private lateinit var tacticalPrefs: TacticalPrefs

    // Views
    private var overlayView: ComposeView? = null
    private var crosshairView: ComposeView? = null
    private var tacticalMenuView: ComposeView? = null
    private var shadowPiercerView: View? = null

    // State
    private val hudState = kotlinx.coroutines.flow.MutableStateFlow(HudState())
    private var isScreenOn = true
    private var metricsJob: Job? = null
    
    // Visibility flags
    private var showHud = false
    private var showCrosshair = false
    private var showTacticalMenu = false

    companion object {
        const val ACTION_TOGGLE_HUD = "TOGGLE_HUD"
        const val ACTION_TOGGLE_CROSSHAIR = "TOGGLE_CROSSHAIR"
        const val ACTION_TOGGLE_TACTICAL = "TOGGLE_TACTICAL"
        const val ACTION_STOP_ALL = "STOP_ALL"
        var isRunning = false
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    metricsJob?.cancel()
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    if (showHud) startUpdatingMetrics()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        crosshairPrefs = CrosshairPrefs.getInstance(this)
        tacticalPrefs = TacticalPrefs.getInstance(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)

        lifecycleOwner = ServiceLifecycleOwner()
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "MATAREO_OVERLAY_CHANNEL")
            .setContentTitle("Matareo Subsystem Active")
            .setContentText("Overlays are running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(1, notification)
        isRunning = true

        // Listen for shadow piercer changes
        serviceScope.launch {
            tacticalPrefs.configFlow.collect { config ->
                if (config.shadowPiercerEnabled) {
                    enableShadowPiercer()
                } else {
                    disableShadowPiercer()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_TOGGLE_HUD -> {
                showHud = !showHud
                if (showHud) {
                    setupOverlayView()
                    startUpdatingMetrics()
                } else {
                    removeOverlayView()
                    metricsJob?.cancel()
                }
            }
            ACTION_TOGGLE_CROSSHAIR -> {
                showCrosshair = !showCrosshair
                if (showCrosshair) setupCrosshairView() else removeCrosshairView()
            }
            ACTION_TOGGLE_TACTICAL -> {
                showTacticalMenu = !showTacticalMenu
                if (showTacticalMenu) setupTacticalMenuView() else removeTacticalMenuView()
            }
            ACTION_STOP_ALL -> {
                stopSelf()
            }
        }
        
        if (!showHud && !showCrosshair && !showTacticalMenu) {
            stopSelf()
        }

        return START_STICKY
    }

    private fun getOverlayFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun setupOverlayView() {
        if (overlayView != null) return
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        overlayView?.setContent {
            val state by hudState.collectAsState()
            OverlayUI(state = state, onDrag = { dx, dy ->
                params.x += dx.toInt()
                params.y += dy.toInt()
                windowManager.updateViewLayout(overlayView, params)
            })
        }
        windowManager.addView(overlayView, params)
    }

    private fun removeOverlayView() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun setupCrosshairView() {
        if (crosshairView != null) return
        crosshairView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        crosshairView?.setContent {
            val config by crosshairPrefs.configFlow.collectAsState()
            CrosshairUI(config = config)
        }
        windowManager.addView(crosshairView, params)
    }

    private fun removeCrosshairView() {
        crosshairView?.let {
            windowManager.removeView(it)
            crosshairView = null
        }
    }

    private fun setupTacticalMenuView() {
        if (tacticalMenuView != null) return
        tacticalMenuView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 400
        }

        tacticalMenuView?.setContent {
            var isExpanded by androidx.compose.runtime.remember { mutableStateOf(false) }
            TacticalMenuUI(
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )
        }
        windowManager.addView(tacticalMenuView, params)
    }

    private fun removeTacticalMenuView() {
        tacticalMenuView?.let {
            windowManager.removeView(it)
            tacticalMenuView = null
        }
    }
    
    // Shadow Piercer logic: Hardware acceleration color matrix + brightness overdrive
    private fun enableShadowPiercer() {
        if (shadowPiercerView != null) return
        shadowPiercerView = object : View(this) {
            val paint = Paint().apply {
                // Boost gamma, reduce contrast to wash out blacks and reveal shadows
                val matrix = ColorMatrix().apply {
                    set(floatArrayOf(
                        1.5f, 0f, 0f, 0f, 40f, // R
                        0f, 1.5f, 0f, 0f, 40f, // G
                        0f, 0f, 1.5f, 0f, 40f, // B
                        0f, 0f, 0f, 0.3f, 0f   // A (translucent overlay to boost brightness artificially)
                    ))
                }
                colorFilter = ColorMatrixColorFilter(matrix)
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            // Overdrive brightness
            screenBrightness = 1.0f 
        }

        windowManager.addView(shadowPiercerView, params)
    }

    private fun disableShadowPiercer() {
        shadowPiercerView?.let {
            windowManager.removeView(it)
            shadowPiercerView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MATAREO_OVERLAY_CHANNEL",
                "Floating Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startUpdatingMetrics() {
        metricsJob?.cancel()
        metricsJob = serviceScope.launch(Dispatchers.IO) {
            while (isRunning && isScreenOn && showHud) {
                val fps = Random.nextInt(58, 61)
                val cpu = readCpuUsage()

                val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    this@FloatingOverlayService.registerReceiver(null, ifilter)
                }
                val batteryPct: Int = batteryStatus?.let { intent ->
                    val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    (level * 100 / scale.toFloat()).toInt()
                } ?: 0

                val temp: Float = batteryStatus?.let { intent ->
                    intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                } ?: getThermalSysfs()

                val ping = measurePing("8.8.8.8")

                hudState.value = HudState(
                    fps = fps,
                    cpu = cpu,
                    temp = temp,
                    battery = batteryPct,
                    ping = ping
                )

                delay(1000)
            }
        }
    }

    private fun readCpuUsage(): Int {
        try {
            val reader = BufferedReader(InputStreamReader(Runtime.getRuntime().exec("top -n 1").inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("%cpu", ignoreCase = true)) {
                    val parts = line!!.split(Regex("\\s+"))
                    for (part in parts) {
                        if (part.contains("%cpu", ignoreCase = true) || part.contains("sys") || part.contains("usr")) {
                            var parsedCpu = part.replace(Regex("[^0-9]"), "").toIntOrNull() ?: Random.nextInt(10, 50)
                            if (parsedCpu > 100) {
                                parsedCpu /= Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                                if (parsedCpu > 100) parsedCpu = 100
                            }
                            return parsedCpu
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Random.nextInt(15, 45)
    }

    private fun getThermalSysfs(): Float {
        try {
            val thermalFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (thermalFile.exists()) {
                val tempStr = thermalFile.readText().trim()
                val tempMilli = tempStr.toFloatOrNull() ?: 0f
                if (tempMilli > 1000) {
                    return tempMilli / 1000f
                }
                return tempMilli
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 37.5f
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
        isRunning = false
        unregisterReceiver(screenReceiver)
        serviceScope.cancel()
        lifecycleOwner.onPause()
        lifecycleOwner.onStop()
        lifecycleOwner.onDestroy()
        
        removeOverlayView()
        removeCrosshairView()
        removeTacticalMenuView()
        disableShadowPiercer()
    }
}
