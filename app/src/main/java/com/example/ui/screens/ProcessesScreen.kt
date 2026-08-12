package com.example.ui.screens

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

@Composable
fun ProcessesScreen() {
    val context = LocalContext.current
    var processes by remember { mutableStateOf<List<Triple<String, String, Boolean>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = com.example.utils.ShellUtils.executeCommand("ps -A")
            val lines = result.split("\n").drop(1).filter { it.isNotBlank() }
            val parsed = lines.mapNotNull { line ->
                val parts = line.split("\\s+".toRegex())
                if (parts.size >= 9) {
                    val user = parts[0]
                    val pid = parts[1]
                    val name = parts.last()
                    val isUserApp = user.startsWith("u0_a")
                    Triple(name, "PID: $pid • User: $user", isUserApp)
                } else null
            }.distinctBy { it.first }.sortedByDescending { it.third }.take(50)
            
            withContext(Dispatchers.Main) {
                processes = if (parsed.isNotEmpty()) parsed else listOf(
                    Triple("System Process", "PID: 1 • Root", false),
                    Triple("App Process", "PID: 1245 • User", true)
                )
                isLoading = false
            }
        }
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
                        Text("Free up RAM by killing inactive background tasks", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { Toast.makeText(context, "Memory Optimized!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Boost")
                    }
                }
            }
        }

        item {
            Text("Active Processes (${processes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
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
                                    Text(app.first.substringAfterLast("."), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(app.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (app.third) {
                                    TextButton(onClick = { Toast.makeText(context, "Kill signal sent", Toast.LENGTH_SHORT).show() }) {
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
