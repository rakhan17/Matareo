package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.utils.ShellUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(navController: NavController) {
    var terminalOutput by remember { mutableStateOf("Matareo Local Shell Executor\nReady. Type a command (e.g. logcat -d, ping 8.8.8.8)...") }
    var terminalInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                // WindowInsets behavior handles the keyboard avoiding for Scaffold content
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = terminalOutput,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = terminalInput,
                onValueChange = { terminalInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Command...") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    val cmd = terminalInput
                    terminalInput = ""
                    terminalOutput += "\n\n> $cmd\nExecuting..."
                    coroutineScope.launch {
                        val res = ShellUtils.executeCommand(cmd)
                        terminalOutput = terminalOutput.replace("Executing...", res)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        val cmd = terminalInput
                        terminalInput = ""
                        terminalOutput += "\n\n> $cmd\nExecuting..."
                        coroutineScope.launch {
                            val res = ShellUtils.executeCommand(cmd)
                            terminalOutput = terminalOutput.replace("Executing...", res)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }) {
                        Icon(Icons.Rounded.Send, contentDescription = "Run")
                    }
                }
            )
        }
    }
}
