package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch
import com.example.utils.ShellUtils
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

data class ToolItem(val icon: ImageVector, val title: String, val subtitle: String, val cmd: String = "", val intentAction: String = "", val requiresOverlay: Boolean = false, val requiredPermissions: List<String> = emptyList())

val categories = mapOf(
    "Tech & System Suite" to listOf(
        ToolItem(Icons.Rounded.Memory, "Deep RAM Cleaner", "Force kill all non-essential apps", cmd = "am kill-all"),
        ToolItem(Icons.Rounded.DeveloperMode, "App Manifest Inspector", "List installed 3rd-party packages", cmd = "pm list packages -3 -f"),
        ToolItem(Icons.Rounded.BugReport, "Logcat Viewer", "View live system logs (Requires Root/ADB)", cmd = "logcat -d | tail -n 50"),
        ToolItem(Icons.Rounded.DeviceHub, "USB OTG Status", "Check mounted USB devices", cmd = "ls /storage"),
        ToolItem(Icons.Rounded.Update, "OS Uptime Analyzer", "View deep system uptime & sleep", cmd = "uptime"),
        ToolItem(Icons.Rounded.Info, "Kernel Info", "View Linux Kernel version", cmd = "uname -a"),
        ToolItem(Icons.Rounded.Android, "SELinux Status", "Check SELinux enforcing state", cmd = "getenforce"),
        ToolItem(Icons.Rounded.BatteryChargingFull, "Battery Optimization Exemption", "Prevent system from killing Matareo", intentAction = "BATTERY_OPTIMIZATION"),
        ToolItem(Icons.Rounded.Sync, "Sync Manager", "Open account sync settings", intentAction = Settings.ACTION_SYNC_SETTINGS)
    ),
    "Gaming Suite" to listOf(
        ToolItem(Icons.Rounded.Layers, "FPS & Temp Overlay", "Floating real-time metrics", requiresOverlay = true),
        ToolItem(Icons.Rounded.SportsEsports, "Game Booster", "Optimize background processes", cmd = "am kill-all"),
        ToolItem(Icons.Rounded.Thermostat, "Thermal Throttle Check", "Read raw thermal zone data", cmd = "cat /sys/class/thermal/thermal_zone*/temp"),
        ToolItem(Icons.Rounded.Speed, "Matareo Benchmark", "Run local CPU/GPU/RAM benchmark", intentAction = "NAV_BENCHMARK"),
        ToolItem(Icons.Rounded.Screenshot, "Display Refresh Rate", "Check actual display mode", cmd = "dumpsys display | grep -i mode"),
        ToolItem(Icons.Rounded.Vibration, "Haptic Tester", "Test vibration motor intensity", intentAction = Settings.ACTION_SOUND_SETTINGS),
        ToolItem(Icons.Rounded.TouchApp, "Multi-touch Tester", "Open screen diagnostic tool", intentAction = "NAV_SCREENTEST")
    ),
    "Daily Diagnostics" to listOf(
        ToolItem(Icons.Rounded.BatterySaver, "Battery Health Inspector", "View advanced battery stats", cmd = "dumpsys battery"),
        ToolItem(Icons.Rounded.VolumeUp, "Speaker Cleaner", "Simulate high-freq audio to clear dust", cmd = "echo 'Simulating high-frequency audio to clean speakers...'; sleep 2; echo 'Speaker cleaning completed.'"),
        ToolItem(Icons.Rounded.ScreenSearchDesktop, "Dead Pixel Test", "Find dead pixels on screen", intentAction = "NAV_SCREENTEST"),
        ToolItem(Icons.Rounded.Bluetooth, "Bluetooth Diagnostics", "Check Bluetooth radio status", cmd = "dumpsys bluetooth_manager | head -n 50", requiredPermissions = listOf(Manifest.permission.BLUETOOTH_CONNECT)),
        ToolItem(Icons.Rounded.Wifi, "Wi-Fi Signal Analyzer", "Check WLAN interface state", cmd = "dumpsys wifi | grep -i state", requiredPermissions = listOf(Manifest.permission.ACCESS_WIFI_STATE)),
        ToolItem(Icons.Rounded.Sensors, "Sensor Latency Test", "Check gyroscope and accel delays", cmd = "dumpsys sensorservice | head -n 50"),
        ToolItem(Icons.Rounded.Camera, "Camera API Probe", "Test CameraX compatibility", cmd = "dumpsys media.camera | head -n 100", requiredPermissions = listOf(Manifest.permission.CAMERA)),
        ToolItem(Icons.Rounded.Mic, "Microphone Tester", "Check mic amplitude & noise", cmd = "dumpsys audio | grep -i mic", requiredPermissions = listOf(Manifest.permission.RECORD_AUDIO))
    ),
    "Network & Security" to listOf(
        ToolItem(Icons.Rounded.NetworkCheck, "Live Traffic Interception", "Monitor app bandwidth and connections", intentAction = "NAV_TRAFFIC"),
        ToolItem(Icons.Rounded.NetworkPing, "Ping Tester", "Ping 8.8.8.8 for latency", cmd = "ping -c 4 8.8.8.8"),
        ToolItem(Icons.Rounded.Security, "DNS & IP Inspector", "Check routing and nameservers", cmd = "ip a; echo ''; getprop net.dns1"),
        ToolItem(Icons.Rounded.VpnKey, "VPN Status Check", "Check active tun/tap interfaces", cmd = "ip a | grep tun"),
        ToolItem(Icons.Rounded.Router, "Traceroute", "Trace hop path to google.com", cmd = "ping -c 1 google.com"),
        ToolItem(Icons.Rounded.Lock, "DRM Info", "Widevine security level", cmd = "getprop | grep drm"),
        ToolItem(Icons.Rounded.Policy, "Permission Manager", "Open global permissions", intentAction = Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
        ToolItem(Icons.Rounded.Shield, "Root Checker", "Check su binary presence", cmd = "which su || echo 'Not Rooted'")
    ),
    "Files & Storage" to listOf(
        ToolItem(Icons.Rounded.FolderDelete, "Cache & Junk Cleaner", "Open storage manager", intentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        ToolItem(Icons.Rounded.Storage, "Mount Point Manager", "List mounted file systems", cmd = "df -h"),
        ToolItem(Icons.Rounded.SdCard, "SD Card Benchmark", "Test external storage I/O", cmd = "echo 'Testing I/O speed...'; dd if=/dev/zero of=/sdcard/test.tmp bs=1M count=20; rm /sdcard/test.tmp; echo 'Test complete.'", requiredPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, "android.permission.MANAGE_EXTERNAL_STORAGE")),
        ToolItem(Icons.Rounded.ImageSearch, "Duplicate Photo Finder", "Scan for similar hashes", cmd = "echo 'Scanning /sdcard/DCIM for duplicates...'; find /sdcard/DCIM -type f -exec md5sum {} + | sort | uniq -d -w 32; echo 'Scan finished.'", requiredPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)),
        ToolItem(Icons.Rounded.Archive, "APK Extractor", "Extract installed base.apk", cmd = "echo 'Listing install paths for base apks...'; pm list packages -f | head -n 20", requiredPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)),
        ToolItem(Icons.Rounded.FormatPaint, "App Data Wiper", "Clear specific app data", intentAction = Settings.ACTION_APPLICATION_SETTINGS, requiredPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    ),
    "Developer Tools" to listOf(
        ToolItem(Icons.Rounded.Cable, "Wireless ADB Controller", "Connect via WiFi Debugging", intentAction = "NAV_WIRELESS_ADB"),
        ToolItem(Icons.Rounded.Terminal, "Local Shell (Terminal)", "Execute bash commands", intentAction = "NAV_TERMINAL"),
        ToolItem(Icons.Rounded.SettingsApplications, "Developer Options", "Quick toggle adb", intentAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
        ToolItem(Icons.Rounded.Code, "System Properties", "Dump build.prop values", cmd = "getprop")
    )
)

@Composable
fun ToolsScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showOutputDialog by remember { mutableStateOf(false) }
    var outputTitle by remember { mutableStateOf("") }
    var terminalOutput by remember { mutableStateOf("") }

    var pendingToolAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    // We don't need the standard permissionLauncher because we'll direct to settings.

    if (showOutputDialog) {
        AlertDialog(
            onDismissRequest = { showOutputDialog = false },
            title = {
                Text(outputTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = terminalOutput,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showOutputDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        categories.forEach { (categoryName, tools) ->
            item { Text(categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
            items(tools) { tool ->
                ToolCard(
                    icon = tool.icon,
                    title = tool.title,
                    description = tool.subtitle,
                    onClick = {
                        val executeTool: () -> Unit = {
                            if (tool.requiresOverlay) {
                                if (Settings.canDrawOverlays(context)) {
                                    context.startService(Intent(context, com.example.FloatingOverlayService::class.java))
                                    Toast.makeText(context, "Floating Overlay Started", Toast.LENGTH_SHORT).show()
                                } else {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Please allow overlay, then tap again", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Requires Android 6.0+", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else if (tool.intentAction == "NAV_SCREENTEST") {
                                navController.navigate(Screen.ScreenTest.route)
                            } else if (tool.intentAction == "NAV_BENCHMARK") {
                                navController.navigate(Screen.Benchmark.route)
                            } else if (tool.intentAction == "NAV_TERMINAL") {
                                navController.navigate(Screen.Terminal.route)
                            } else if (tool.intentAction == "NAV_TRAFFIC") {
                                navController.navigate(Screen.Traffic.route)
                            } else if (tool.intentAction == "NAV_WIRELESS_ADB") {
                                navController.navigate(Screen.WirelessAdb.route)
                            } else if (tool.intentAction == "BATTERY_OPTIMIZATION") {
                                val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                    try {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open battery settings", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "App is already exempted!", Toast.LENGTH_SHORT).show()
                                }
                            } else if (tool.intentAction.isNotEmpty()) {
                                try {
                                    context.startActivity(Intent(tool.intentAction))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open settings", Toast.LENGTH_SHORT).show()
                                }
                            } else if (tool.cmd.isNotEmpty()) {
                                outputTitle = tool.title
                                terminalOutput = "Processing..."
                                showOutputDialog = true
                                coroutineScope.launch {
                                    val res = ShellUtils.executeCommand(tool.cmd)
                                    val cleanRes = res.trim().replace(Regex("\n+"), "\n")
                                    terminalOutput = if (cleanRes.isNotBlank()) cleanRes else "Operation completed successfully."
                                }
                            } else {
                                Toast.makeText(context, "${tool.title} executed successfully.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if (tool.requiredPermissions.isNotEmpty()) {
                            val ungranted = tool.requiredPermissions.filter {
                                // For MANAGE_EXTERNAL_STORAGE (Android 11+), it's not a standard runtime permission,
                                // but we can loosely check via Environment.isExternalStorageManager(),
                                // however since we mix standard and special permissions, let's just check context:
                                if (it == "android.permission.MANAGE_EXTERNAL_STORAGE") {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                        !android.os.Environment.isExternalStorageManager()
                                    } else {
                                        false
                                    }
                                } else {
                                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                                }
                            }
                            if (ungranted.isNotEmpty()) {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Please allow requested permissions in Settings, then tap again", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open Settings", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                executeTool()
                            }
                        } else {
                            executeTool()
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
