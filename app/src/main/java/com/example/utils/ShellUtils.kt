package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object ShellUtils {
    suspend fun executeCommand(command: String): String {
        return withContext(Dispatchers.IO) {
            val output = StringBuilder()
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                while (errorReader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                
                process.waitFor()
            } catch (e: Exception) {
                output.append("Error executing command: ${e.message}\n")
            }
            output.toString().trim()
        }
    }
}
