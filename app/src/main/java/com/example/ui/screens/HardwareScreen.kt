package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.SystemStats
import java.io.File
import java.net.NetworkInterface

@Composable
fun HardwareScreen(stats: SystemStats) {
    val context = LocalContext.current

    // Fetch real metrics
    val displayInfo = remember {
        val displayMetrics = context.resources.displayMetrics
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val refresh = display.refreshRate
        Pair("${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}", "${refresh.toInt()} Hz")
    }

    val sensorInfo = remember {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
        val light = sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
        val prox = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
        listOf(acc, gyro, light, mag, prox)
    }

    val cameraInfo = remember {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cams = cm.cameraIdList
            if (cams.isNotEmpty()) "${cams.size} Cameras Detected" else "No Camera API Access"
        } catch (e: Exception) {
            "Restricted"
        }
    }

    val networkInfo = remember {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var ip = "Offline"
            for (intf in interfaces) {
                if (intf.name.contains("wlan") || intf.name.contains("rmnet")) {
                    for (addr in intf.inetAddresses) {
                        if (!addr.isLoopbackAddress && addr.hostAddress?.contains(":") == false) {
                            ip = addr.hostAddress ?: "Unknown"
                        }
                    }
                }
            }
            ip
        } catch (e: Exception) { "Unknown" }
    }

    val board = remember { Build.BOARD }
    
    val cpuFreqs = remember {
        val cores = Runtime.getRuntime().availableProcessors()
        var maxFreq = 0L
        for (i in 0 until cores) {
            try {
                val freq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLongOrNull() ?: 0L
                if (freq > maxFreq) maxFreq = freq
            } catch (e: Exception) {}
        }
        if (maxFreq > 0) "${maxFreq / 1000} MHz" else "Unknown (Blocked by OS)"
    }

    val batterySysfs = remember {
        var cycle = "Unknown"
        var current = "Unknown"
        try {
            val cycleFile = File("/sys/class/power_supply/battery/cycle_count")
            if (cycleFile.exists()) cycle = cycleFile.readText().trim()
            val currentFile = File("/sys/class/power_supply/battery/current_now")
            if (currentFile.exists()) current = "${currentFile.readText().trim().toLongOrNull()?.div(1000)} mA"
        } catch (e: Exception) {}
        Pair(cycle, current)
    }
    
    val realRamInfo = remember {
        try {
            val meminfo = File("/proc/meminfo").readLines()
            val memTotalLine = meminfo.firstOrNull { it.startsWith("MemTotal:") }
            if (memTotalLine != null) {
                val kb = memTotalLine.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                val gb = kb / 1024.0 / 1024.0
                String.format("%.2f GB Total Hardware RAM", gb)
            } else "Unknown"
        } catch (e: Exception) { "Unknown" }
    }
    
    val realCpuHardware = remember {
        try {
            val cpuinfo = File("/proc/cpuinfo").readLines()
            val hardwareLine = cpuinfo.firstOrNull { it.startsWith("Hardware") }
            hardwareLine?.substringAfter(":")?.trim() ?: Build.HARDWARE
        } catch (e: Exception) { Build.HARDWARE }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            HardwareSectionCard("Device & OS Identity", Icons.Rounded.Smartphone) {
                InspectorItem(Icons.Rounded.Info, "Market Name", stats.deviceName)
                InspectorItem(Icons.Rounded.Android, "OS Version", stats.osVersion)
                InspectorItem(Icons.Rounded.Label, "Model (Build.MODEL)", Build.MODEL)
                InspectorItem(Icons.Rounded.Factory, "Manufacturer", Build.MANUFACTURER)
                InspectorItem(Icons.Rounded.PrecisionManufacturing, "Device Code", Build.DEVICE)
                InspectorItem(Icons.Rounded.DeveloperBoard, "Board", board)
            }
        }
        
        item {
            HardwareSectionCard("Anti-Fake Detection (Real Hardware)", Icons.Rounded.Security) {
                InspectorItem(Icons.Rounded.Memory, "Real Physical RAM", realRamInfo)
                InspectorItem(Icons.Rounded.Memory, "SoC / Actual Hardware", realCpuHardware)
            }
        }

        item {
            HardwareSectionCard("Battery SysFS (Raw Data)", Icons.Rounded.BatteryChargingFull) {
                InspectorItem(Icons.Rounded.BatteryStd, "Capacity Current", "${stats.batteryPercent}%")
                InspectorItem(Icons.Rounded.Loop, "Cycle Count", batterySysfs.first)
                InspectorItem(Icons.Rounded.Bolt, "Charge Current", batterySysfs.second)
                InspectorItem(Icons.Rounded.Thermostat, "Raw Temp", "${stats.batteryTemp * 10} (sysfs raw)")
            }
        }

        item {
            HardwareSectionCard("CPU Cores & GPU", Icons.Rounded.Memory) {
                InspectorItem(Icons.Rounded.Memory, "Total Cores", "${Runtime.getRuntime().availableProcessors()} Cores")
                InspectorItem(Icons.Rounded.Speed, "Max Frequency", cpuFreqs)
                InspectorItem(Icons.Rounded.DeveloperBoard, "Supported ABIs", Build.SUPPORTED_ABIS.joinToString(", "))
            }
        }

        item {
            HardwareSectionCard("Network (Local Interfaces)", Icons.Rounded.Wifi) {
                InspectorItem(Icons.Rounded.Router, "Local IP Address", networkInfo)
                InspectorItem(Icons.Rounded.CellTower, "Radio Version", Build.getRadioVersion() ?: "Unknown")
            }
        }
        
        item {
            HardwareSectionCard("Display & Cameras", Icons.Rounded.ScreenshotMonitor) {
                InspectorItem(Icons.Rounded.Wallpaper, "Resolution", displayInfo.first)
                InspectorItem(Icons.Rounded.Speed, "Refresh Rate", displayInfo.second)
                InspectorItem(Icons.Rounded.Camera, "Camera API 2", cameraInfo)
            }
        }
        
        item {
            HardwareSectionCard("Sensors", Icons.Rounded.Explore) {
                InspectorItem(Icons.Rounded.ScreenRotation, "Accelerometer", if (sensorInfo[0]) "Available" else "Not Present")
                InspectorItem(Icons.Rounded.Sync, "Gyroscope", if (sensorInfo[1]) "Available" else "Not Present")
                InspectorItem(Icons.Rounded.WbSunny, "Light Sensor", if (sensorInfo[2]) "Available" else "Not Present")
                InspectorItem(Icons.Rounded.CompassCalibration, "Magnetometer", if (sensorInfo[3]) "Available" else "Not Present")
                InspectorItem(Icons.Rounded.Sensors, "Proximity", if (sensorInfo[4]) "Available" else "Not Present")
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun HardwareSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
fun InspectorItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
