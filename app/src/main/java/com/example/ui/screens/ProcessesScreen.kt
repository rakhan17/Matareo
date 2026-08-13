package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppProcessInfo(val name: String, val packageName: String, val isUserApp: Boolean)

@Composable
fun ProcessesScreen() {
    val context = LocalContext.current
    var processes by remember { mutableStateOf<List<AppProcessInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun fetchProcesses() {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val parsed = packages.mapNotNull { appInfo ->
                if (appInfo.packageName == context.packageName) return@mapNotNull null
                
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val appName = pm.getApplicationLabel(appInfo).toString()
                
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null || !isSystem) {
                    AppProcessInfo(appName, appInfo.packageName, !isSystem)
                } else null
            }.sortedByDescending { it.isUserApp }
            
            withContext(Dispatchers.Main) {
                processes = parsed
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchProcesses()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Memory Optimizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Free up RAM by killing background tasks", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { 
                        coroutineScope.launch(Dispatchers.IO) {
                            processes.filter { it.isUserApp }.forEach { app ->
                                activityManager.killBackgroundProcesses(app.packageName)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Memory Optimized!", Toast.LENGTH_SHORT).show()
                                fetchProcesses()
                            }
                        }
                    }) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Boost")
                    }
                }
            }
        }

        item {
            Text("App Processes (${processes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        processes.forEach { app ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (app.isUserApp) {
                                    TextButton(onClick = { 
                                        activityManager.killBackgroundProcesses(app.packageName)
                                        Toast.makeText(context, "${app.name} force stopped", Toast.LENGTH_SHORT).show()
                                        fetchProcesses()
                                    }) {
                                        Text("Force Stop", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                    }
                                } else {
                                    Text("System", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(end = 16.dp))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
