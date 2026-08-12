package com.example

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.max
import kotlin.random.Random

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.example.utils.ShellUtils
import java.io.BufferedReader
import java.io.InputStreamReader

data class SystemStats(
    // Hardware Info
    val deviceName: String,
    val osVersion: String,
    val hardware: String,

    // Battery
    val batteryTemp: Float,
    val batteryVoltage: Float,
    val batteryHealth: String,
    val batteryPercent: Float,
    val isCharging: Boolean,

    // RAM
    val ramUsedPercent: Float,
    val ramTotalMb: Long,
    val ramUsedMb: Long,

    // Storage
    val storageTotalGb: Float,
    val storageUsedGb: Float,
    val storageFreeGb: Float,

    // Network
    val downloadSpeedKbps: Float,
    val uploadSpeedKbps: Float,
    val wifiDataUsedGb: Float, 
    val cellularDataUsedMb: Float, 
    val pingMs: Int, 

    // CPU / GPU
    val cpuUsagePercent: Float,
    val gpuUsagePercent: Float
)

class SystemStatsRepository(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTimeMs = System.currentTimeMillis()

    fun getStatsFlow(): Flow<SystemStats> = flow {
        while (true) {
            val stats = fetchStats()
            emit(stats)
            delay(1000)
        }
    }

    private suspend fun fetchStats(): SystemStats = withContext(Dispatchers.IO) {
        // Hardware
        val deviceName = Build.MODEL ?: "Unknown Device"
        val osVersion = "Android ${Build.VERSION.RELEASE}"
        val hardware = Build.HARDWARE ?: "Unknown Chip"

        // RAM
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        val availRamMb = memoryInfo.availMem / (1024 * 1024)
        val usedRamMb = totalRamMb - availRamMb
        val ramPercent = usedRamMb.toFloat() / totalRamMb.toFloat()

        // Storage
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.totalBytes
        val availableBytes = statFs.availableBytes
        val usedBytes = totalBytes - availableBytes
        
        val totalGb = totalBytes / (1024f * 1024f * 1024f)
        val freeGb = availableBytes / (1024f * 1024f * 1024f)
        val usedGb = usedBytes / (1024f * 1024f * 1024f)

        // Network
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTimeMs = System.currentTimeMillis()

        val timeDiff = (currentTimeMs - lastTimeMs) / 1000f // seconds
        var dlSpeedKbps = 0f
        var ulSpeedKbps = 0f

        if (timeDiff > 0 && currentTxBytes != TrafficStats.UNSUPPORTED.toLong()) {
            val rxDiff = currentRxBytes - lastRxBytes
            val txDiff = currentTxBytes - lastTxBytes
            dlSpeedKbps = (max(0L, rxDiff) / timeDiff) / 1024f
            ulSpeedKbps = (max(0L, txDiff) / timeDiff) / 1024f
        }

        lastTxBytes = currentTxBytes
        lastRxBytes = currentRxBytes
        lastTimeMs = currentTimeMs

        // Battery
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level != -1 && scale != -1) {
            level * 100f / scale.toFloat()
        } else {
            100f
        }
        
        val temp = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        
        val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val batteryHealth = when(healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        // Real CPU Load calculation (average across active cores via scaling_cur_freq / cpuinfo_max_freq)
        val numCores = Runtime.getRuntime().availableProcessors()
        var totalCpuFreq = 0f
        var activeCores = 0
        for (i in 0 until numCores) {
            try {
                val freqStr = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim()
                val maxFreqStr = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim()
                if (freqStr.isNotEmpty() && maxFreqStr.isNotEmpty()) {
                    val freqKHz = freqStr.toLongOrNull() ?: 0L
                    val maxKHz = maxFreqStr.toLongOrNull() ?: 1L
                    if (maxKHz > 0) {
                        totalCpuFreq += (freqKHz.toFloat() / maxKHz.toFloat())
                        activeCores++
                    }
                }
            } catch (e: Exception) {
                // Ignore, core might be offline
            }
        }
        val currentCpu = if (activeCores > 0) (totalCpuFreq / activeCores) * 100f else 0f
        
        // Ping
        var pingMs = -1
        try {
            val process = Runtime.getRuntime().exec(arrayOf("ping", "-c", "1", "-W", "1", "8.8.8.8"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            val match = "time=([0-9.]+)".toRegex().find(output)
            if (match != null) {
                pingMs = match.groupValues[1].toFloat().toInt()
            }
        } catch (e: Exception) {}

        // GPU Load Calculation (Read from SysFS)
        var currentGpu = -1f
        try {
            val adrenoPath = File("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
            val maliPath = File("/sys/class/devfreq/gpufreq/device/load")
            
            if (adrenoPath.exists()) {
                val str = adrenoPath.readText().trim()
                currentGpu = str.removeSuffix("%").toFloatOrNull() ?: -1f
            } else if (maliPath.exists()) {
                val str = maliPath.readText().trim()
                // Mali typically formats like "X@YHz"
                val loadPart = str.split("@").firstOrNull() ?: ""
                currentGpu = loadPart.toFloatOrNull() ?: -1f
            }
        } catch (e: Exception) {}

        SystemStats(
            deviceName = deviceName,
            osVersion = osVersion,
            hardware = hardware,
            batteryTemp = temp,
            batteryVoltage = voltage,
            batteryHealth = batteryHealth,
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            ramUsedPercent = ramPercent,
            ramTotalMb = totalRamMb,
            ramUsedMb = usedRamMb,
            storageTotalGb = totalGb,
            storageUsedGb = usedGb,
            storageFreeGb = freeGb,
            downloadSpeedKbps = dlSpeedKbps,
            uploadSpeedKbps = ulSpeedKbps,
            wifiDataUsedGb = 0f, 
            cellularDataUsedMb = 0f,
            pingMs = pingMs,
            cpuUsagePercent = currentCpu,
            gpuUsagePercent = currentGpu
        )
    }
}
