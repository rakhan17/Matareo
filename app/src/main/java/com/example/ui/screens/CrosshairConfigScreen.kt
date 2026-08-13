package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.utils.CrosshairConfig
import com.example.utils.CrosshairPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosshairConfigScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val prefs = remember { CrosshairPrefs.getInstance(context) }
    val config by prefs.configFlow.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            prefs.updateConfig(config.copy(imageUri = it.toString(), preset = 0))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crosshair Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Selection
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Style", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("Dot", 1, config.preset) { prefs.updateConfig(config.copy(preset = 1, imageUri = "")) }
                    PresetButton("Cross", 2, config.preset) { prefs.updateConfig(config.copy(preset = 2, imageUri = "")) }
                    PresetButton("Circle", 3, config.preset) { prefs.updateConfig(config.copy(preset = 3, imageUri = "")) }
                    PresetButton("Hollow", 4, config.preset) { prefs.updateConfig(config.copy(preset = 4, imageUri = "")) }
                    PresetButton("Blue", 5, config.preset) { prefs.updateConfig(config.copy(preset = 5, imageUri = "")) }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Custom Image from Gallery")
                }

                if (config.imageUri.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(config.imageUri)),
                        contentDescription = "Selected",
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Sliders
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Adjustments", fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Scale: ${String.format("%.1f", config.scale)}x")
                Slider(
                    value = config.scale,
                    onValueChange = { prefs.updateConfig(config.copy(scale = it)) },
                    valueRange = 0.5f..3f
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Opacity (Alpha): ${String.format("%.1f", config.alpha)}")
                Slider(
                    value = config.alpha,
                    onValueChange = { prefs.updateConfig(config.copy(alpha = it)) },
                    valueRange = 0.1f..1f
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("X Offset: ${config.offsetX.toInt()}dp")
                Slider(
                    value = config.offsetX,
                    onValueChange = { prefs.updateConfig(config.copy(offsetX = it)) },
                    valueRange = -300f..300f
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Y Offset: ${config.offsetY.toInt()}dp")
                Slider(
                    value = config.offsetY,
                    onValueChange = { prefs.updateConfig(config.copy(offsetY = it)) },
                    valueRange = -300f..300f
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { prefs.updateConfig(CrosshairConfig(preset = config.preset, imageUri = config.imageUri)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Adjustments")
                }
            }
            }
        }
    }
}

@Composable
fun PresetButton(label: String, id: Int, current: Int, onClick: () -> Unit) {
    val isSelected = id == current
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
