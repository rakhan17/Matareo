package com.example.ui.screens

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.example.utils.ShellUtils

data class AppTraffic(
    val uid: Int,
    val appName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val rxSpeed: Long = 0,
    val txSpeed: Long = 0,
    val connections: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficScreen(navController: NavController) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var trafficList by remember { mutableStateOf<List<AppTraffic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val pm = context.packageManager
        
        var previousMap = mutableMapOf<Int, AppTraffic>()

        withContext(Dispatchers.IO) {
            while (isActive) {
                val currentMap = mutableMapOf<Int, AppTraffic>()
                try {
                    val bucket = NetworkStats.Bucket()
                    // Query for Wi-Fi and Mobile
                    val networks = listOf(ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE)
                    val startTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 1 day history

                    for (networkType in networks) {
                        val stats = networkStatsManager.querySummary(networkType, null, startTime, System.currentTimeMillis())
                        while (stats.hasNextBucket()) {
                            stats.getNextBucket(bucket)
                            val uid = bucket.uid
                            if (uid < 10000) continue // Skip system uids for now
                            
                            val existing = currentMap[uid]
                            currentMap[uid] = AppTraffic(
                                uid = uid,
                                appName = "",
                                rxBytes = (existing?.rxBytes ?: 0) + bucket.rxBytes,
                                txBytes = (existing?.txBytes ?: 0) + bucket.txBytes
                            )
                        }
                        stats.close()
                    }

                    // Get TCP connections using netstat
                    val netstatOutput = ShellUtils.executeCommand("netstat -tunp")
                    val connectionsByUid = parseNetstat(netstatOutput)

                    // Calculate speeds and populate names
                    val finalList = currentMap.map { (uid, currentTraffic) ->
                        val prev = previousMap[uid]
                        val rxSpeed = if (prev != null) Math.max(0L, currentTraffic.rxBytes - prev.rxBytes) else 0L
                        val txSpeed = if (prev != null) Math.max(0L, currentTraffic.txBytes - prev.txBytes) else 0L
                        
                        var name = "Unknown"
                        val packages = pm.getPackagesForUid(uid)
                        if (!packages.isNullOrEmpty()) {
                            try {
                                val ai = pm.getApplicationInfo(packages[0], 0)
                                name = pm.getApplicationLabel(ai).toString()
                            } catch (e: Exception) {
                                name = packages[0]
                            }
                        }

                        currentTraffic.copy(
                            appName = name,
                            rxSpeed = rxSpeed,
                            txSpeed = txSpeed,
                            connections = connectionsByUid[uid] ?: emptyList()
                        )
                    }.filter { it.rxBytes > 0 || it.txBytes > 0 }
                    .sortedByDescending { it.rxSpeed + it.txSpeed }

                    withContext(Dispatchers.Main) {
                        trafficList = finalList
                        isLoading = false
                    }
                    
                    previousMap = currentMap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000) // update every second
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Traffic Interception") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            if (!hasPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Permission Required", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("To view live app traffic, we need 'Usage Access' permission. This allows us to read network stats.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { hasPermission = checkUsageStatsPermission(context) }, colors = ButtonDefaults.outlinedButtonColors()) {
                            Text("I've Granted It")
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text("Real-Time Bandwidth & Connections", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trafficList) { app ->
                        TrafficAppRow(app)
                    }
                }
            }
        }
    }
}

@Composable
fun TrafficAppRow(app: AppTraffic) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.appName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("UID: ${app.uid}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Down: ${formatBytes(app.rxBytes)}", fontSize = 12.sp)
                    Text("Total Up: ${formatBytes(app.txBytes)}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("↓ ${formatBytes(app.rxSpeed)}/s", fontSize = 14.sp, color = Color.Green)
                    Text("↑ ${formatBytes(app.txSpeed)}/s", fontSize = 14.sp, color = Color.Red)
                }
            }
            if (app.connections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Active IPs:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                app.connections.forEach { ip ->
                    Text(ip, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.Yellow)
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}

private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun parseNetstat(output: String): Map<Int, List<String>> {
    val result = mutableMapOf<Int, MutableList<String>>()
    val lines = output.split("\n")
    for (line in lines) {
        val parts = line.split("\\s+".toRegex())
        if (parts.size >= 7 && (parts[0] == "tcp" || parts[0] == "tcp6" || parts[0] == "udp" || parts[0] == "udp6")) {
            val foreignAddress = parts[4]
            // Extract UID - usually the 7th or 8th column depending on state. In android netstat -tunp, UID is usually column 7.
            // Let's try to find an integer after state
            var uid = -1
            for (p in parts) {
                if (p.toIntOrNull() != null && p.toIntOrNull()!! > 1000) {
                    uid = p.toIntOrNull()!!
                    break
                }
            }
            if (uid != -1 && foreignAddress != "0.0.0.0:*") {
                if (!result.containsKey(uid)) result[uid] = mutableListOf()
                if (!result[uid]!!.contains(foreignAddress)) {
                    result[uid]!!.add(foreignAddress)
                }
            }
        }
    }
    return result
}
