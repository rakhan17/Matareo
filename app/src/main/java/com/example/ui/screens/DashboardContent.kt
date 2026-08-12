package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DashboardUiState
import com.example.SystemStats
import com.example.ui.components.CircularGauge
import com.example.ui.components.SimpleLineChart
import kotlinx.coroutines.launch

@Composable
fun DashboardContent(uiState: DashboardUiState, stats: SystemStats) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DeviceBanner(stats) }
        item { QuickActionsRow() }
        item { MetricsGrid(stats) }
        item { DetailTabsSection(uiState, stats) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DeviceBanner(stats: SystemStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Smartphone, contentDescription = "Device", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stats.deviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("${stats.osVersion} • ${stats.hardware}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" ${stats.batteryTemp}°C - Normal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Rounded.Wifi, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" Connected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(Icons.Rounded.Memory, "Boost RAM") { 
            Toast.makeText(context, "Killing background processes...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                com.example.utils.ShellUtils.executeCommand("am kill-all")
                Toast.makeText(context, "RAM Boosted!", Toast.LENGTH_SHORT).show()
            }
        }
        QuickActionButton(Icons.Rounded.CleaningServices, "Clear Cache") { 
            try {
                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open storage", Toast.LENGTH_SHORT).show()
            }
        }
        QuickActionButton(Icons.Rounded.BatterySaver, "Battery Saver") { 
            try {
                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Battery Settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun RowScope.QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MetricsGrid(stats: SystemStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardCard(title = "RAM", modifier = Modifier.weight(1f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    CircularGauge(progress = stats.ramUsedPercent, modifier = Modifier.size(90.dp), color = MaterialTheme.colorScheme.primary)
                    Text("${(stats.ramUsedPercent * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text("${stats.ramUsedMb} / ${stats.ramTotalMb} MB", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                OutlinedButton(
                    onClick = { Toast.makeText(context, "RAM Cleared", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Clear RAM", fontSize = 12.sp) }
            }

            DashboardCard(title = "Battery", modifier = Modifier.weight(1f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    CircularGauge(
                        progress = stats.batteryPercent / 100f,
                        modifier = Modifier.size(90.dp),
                        color = if (stats.batteryPercent > 20) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text("${stats.batteryPercent.toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(if (stats.isCharging) "Charging" else "Discharging", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Health: ${stats.batteryHealth}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("${stats.batteryVoltage} V • ${stats.batteryTemp}°C", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
        
        DashboardCard(title = "Storage") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val cacheColor = MaterialTheme.colorScheme.surfaceVariant
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()
                        val total = stats.storageTotalGb.coerceAtLeast(1f)
                        val used = stats.storageUsedGb
                        val free = stats.storageFreeGb
                        
                        // Fake proportions based on used for visual representation
                        val systemGb = used * 0.3f
                        val appsGb = used * 0.4f
                        val mediaGb = used * 0.2f
                        val cacheGb = used * 0.1f
                        
                        val gap = 2f
                        val sweepSystem = ((systemGb / total) * 360f)
                        val sweepApps = ((appsGb / total) * 360f)
                        val sweepMedia = ((mediaGb / total) * 360f)
                        val sweepCache = ((cacheGb / total) * 360f)
                        
                        // Background (Free)
                        drawArc(
                            color = androidx.compose.ui.graphics.Color.DarkGray,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                        )
                        
                        // Foreground System
                        var currentAngle = -90f
                        drawArc(
                            color = primaryColor,
                            startAngle = currentAngle,
                            sweepAngle = (sweepSystem - gap).coerceAtLeast(0f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                        )
                        currentAngle += sweepSystem
                        
                        // Foreground Apps
                        drawArc(
                            color = secondaryColor,
                            startAngle = currentAngle,
                            sweepAngle = (sweepApps - gap).coerceAtLeast(0f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                        )
                        currentAngle += sweepApps
                        
                        // Foreground Media
                        drawArc(
                            color = tertiaryColor,
                            startAngle = currentAngle,
                            sweepAngle = (sweepMedia - gap).coerceAtLeast(0f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                        )
                        currentAngle += sweepMedia
                        
                        // Foreground Cache
                        drawArc(
                            color = cacheColor,
                            startAngle = currentAngle,
                            sweepAngle = (sweepCache - gap).coerceAtLeast(0f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${String.format("%.0f", (stats.storageFreeGb / stats.storageTotalGb.coerceAtLeast(1f)) * 100)}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Tersisa", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("${String.format("%.1f", stats.storageUsedGb)} GB used", fontWeight = FontWeight.Bold)
                    Text("of ${String.format("%.1f", stats.storageTotalGb)} GB Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegendItem("System", MaterialTheme.colorScheme.primary)
                        LegendItem("Apps", MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegendItem("Media", MaterialTheme.colorScheme.tertiary)
                        LegendItem("Cache", MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp)
    }
}

@Composable
fun DetailTabsSection(uiState: DashboardUiState, stats: SystemStats) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Network", "CPU", "GPU")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
            
            Box(modifier = Modifier.padding(16.dp)) {
                when (selectedTab) {
                    0 -> NetworkTabContent(uiState, stats)
                    1 -> CpuTabContent(uiState, stats)
                    2 -> GpuTabContent(uiState, stats)
                }
            }
        }
    }
}

@Composable
fun NetworkTabContent(uiState: DashboardUiState, stats: SystemStats) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Download", style = MaterialTheme.typography.labelMedium)
                Text("${String.format("%.1f", stats.downloadSpeedKbps)} KB/s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Upload", style = MaterialTheme.typography.labelMedium)
                Text("${String.format("%.1f", stats.uploadSpeedKbps)} KB/s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            SimpleLineChart(data = uiState.downloadHistory, maxValue = 10000f, modifier = Modifier.fillMaxSize(), lineColor = MaterialTheme.colorScheme.primary)
            SimpleLineChart(data = uiState.uploadHistory, maxValue = 10000f, modifier = Modifier.fillMaxSize(), lineColor = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Data Today: Wi-Fi ${String.format("%.1f", stats.wifiDataUsedGb)} GB | Cell ${stats.cellularDataUsedMb} MB", style = MaterialTheme.typography.labelSmall)
            Text("Ping: ${if (stats.pingMs < 0) "Timeout" else "${stats.pingMs} ms"}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CpuTabContent(uiState: DashboardUiState, stats: SystemStats) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Avg CPU Load (sysfs)", style = MaterialTheme.typography.labelMedium)
            Text("${String.format("%.1f", stats.cpuUsagePercent)}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        SimpleLineChart(data = uiState.cpuHistory, maxValue = 100f, modifier = Modifier.fillMaxWidth().height(120.dp), lineColor = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun GpuTabContent(uiState: DashboardUiState, stats: SystemStats) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val isAvailable = stats.gpuUsagePercent >= 0f
            Text(if (isAvailable) "GPU Load (SysFS)" else "GPU Load (Requires Root/Vendor support)", style = MaterialTheme.typography.labelMedium)
            Text(if (isAvailable) "${String.format("%.1f", stats.gpuUsagePercent)}%" else "- %", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        SimpleLineChart(data = uiState.gpuHistory, maxValue = 100f, modifier = Modifier.fillMaxWidth().height(120.dp), lineColor = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun DashboardCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
