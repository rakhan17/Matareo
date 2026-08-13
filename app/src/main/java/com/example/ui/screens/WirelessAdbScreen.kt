package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.utils.AdbClientProtocol
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessAdbScreen(navController: NavController) {
    var port by remember { mutableStateOf("") }
    var pairingCode by remember { mutableStateOf("") }
    var outputLog by remember { mutableStateOf(listOf("Welcome to Matareo Wireless ADB Controller.", "Please enable Wireless Debugging in Developer Options.")) }
    var command by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    var isConnecting by remember { mutableStateOf(false) }

    fun addLog(msg: String) {
        outputLog = outputLog + msg
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wireless ADB Controller") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Connection Setup", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port (e.g. 42315)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pairingCode,
                            onValueChange = { pairingCode = it },
                            label = { Text("Pairing Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (port.isBlank() || pairingCode.isBlank()) {
                                    addLog("Error: Port and Pairing Code required for pairing.")
                                    return@Button
                                }
                                isConnecting = true
                                addLog("Attempting to pair with 127.0.0.1:$port...")
                                coroutineScope.launch {
                                    val (success, msg) = AdbClientProtocol.pair("127.0.0.1", port.toIntOrNull() ?: 5555, pairingCode)
                                    addLog(if (success) "Pairing Success: $msg" else "Pairing Failed: $msg")
                                    isConnecting = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isConnecting && !AdbClientProtocol.isConnected
                        ) {
                            Text("Pair Device")
                        }
                        
                        Button(
                            onClick = {
                                if (port.isBlank()) {
                                    addLog("Error: Port required for connecting.")
                                    return@Button
                                }
                                isConnecting = true
                                addLog("Attempting to connect to 127.0.0.1:$port...")
                                coroutineScope.launch {
                                    val (success, msg) = AdbClientProtocol.connect("127.0.0.1", port.toIntOrNull() ?: 5555)
                                    addLog(if (success) "Connected: $msg" else "Connection Failed: $msg")
                                    isConnecting = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isConnecting && !AdbClientProtocol.isConnected,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Terminal Output
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(outputLog) { log ->
                        Text(log, color = Color(0xFF58A6FF), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Command Input
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("adb shell command (e.g. wm size)") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (command.isNotBlank()) {
                                val currentCmd = command
                                command = ""
                                addLog("matareo@adb:~$ $currentCmd")
                                coroutineScope.launch {
                                    val res = AdbClientProtocol.executeShell(currentCmd)
                                    addLog(res)
                                }
                            }
                        },
                        enabled = AdbClientProtocol.isConnected
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = "Send")
                    }
                },
                singleLine = true,
                enabled = AdbClientProtocol.isConnected
            )
        }
    }
}
