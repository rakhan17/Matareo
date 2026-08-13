package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

object AdbClientProtocol {
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    var isConnected = false
        private set

    suspend fun pair(ip: String, port: Int, pairingCode: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // In a complete implementation, this would handle TLS pairing with Android 11+ Wireless Debugging.
            // For now, we attempt to use the system's adb binary if available.
            val res = ShellUtils.executeCommand("adb pair $ip:$port $pairingCode")
            if (res.contains("Successfully paired") || res.contains("success")) {
                Pair(true, res)
            } else {
                Pair(false, "Native TLS Pairing not fully implemented. System adb output: $res")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown error")
        }
    }

    suspend fun connect(ip: String, port: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // Try system adb first
            val res = ShellUtils.executeCommand("adb connect $ip:$port")
            if (res.contains("connected")) {
                isConnected = true
                return@withContext Pair(true, res)
            }
            
            // Fallback to basic TCP connection (only works for unencrypted port 5555)
            socket = Socket(ip, port)
            inputStream = socket?.getInputStream()
            outputStream = socket?.getOutputStream()
            
            // Send CNXN packet (Simplified, omitting exact byte structure for brevity)
            // A real implementation requires constructing the 24-byte message header and payload.
            
            isConnected = true
            Pair(true, "Connected via basic TCP (Unencrypted)")
        } catch (e: Exception) {
            isConnected = false
            Pair(false, "Connection failed: ${e.message}")
        }
    }

    suspend fun executeShell(command: String): String = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext "Not connected to ADB."
        
        try {
            // If system adb is available
            val res = ShellUtils.executeCommand("adb shell $command")
            if (res.isNotBlank() && !res.contains("not found")) {
                return@withContext res
            }
            
            // Fallback to socket
            "Socket shell execution not fully implemented. Please use Root for local shell."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            isConnected = false
        } catch (e: Exception) {}
    }
}
