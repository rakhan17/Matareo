package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var isDarkMode by remember { mutableStateOf(true) }
    var showFpsOverlay by remember { mutableStateOf(false) }
    var smartAlerts by remember { mutableStateOf(true) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item { Text("Permissions & Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.Security,
                    title = "App Permissions Manager",
                    subtitle = "Grant access to storage, overlay, or usage stats",
                    onClick = { 
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    actionIcon = Icons.Rounded.OpenInNew
                )
            }
        }

        item { Text("Local Backups (No Cloud)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.CloudSync,
                    title = "Export Local Dashboard Data",
                    subtitle = "Save current stats to clipboard",
                    onClick = { 
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Dashboard Data", "Local Dashboard backup created at ${System.currentTimeMillis()}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsClickableItem(
                    icon = Icons.Rounded.Backup,
                    title = "System Backup Settings",
                    subtitle = "Open Android native backup",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open Backup Settings", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        item { Text("System Automations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "Display & Brightness",
                    subtitle = "Manage screen dimming and profiles",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open Display Settings", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsClickableItem(
                    icon = Icons.Rounded.Schedule,
                    title = "Developer Options",
                    subtitle = "Access deep system controls",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Developer options not enabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        item { Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Toggle dark/light theme (System)",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                        } catch (e: Exception) {}
                    }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsClickableItem(
                    icon = Icons.Rounded.Palette,
                    title = "Wallpaper & Style",
                    subtitle = "Change dynamic accent colors",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
                        } catch (e: Exception) {}
                    }
                )
            }
        }

        item { Text("Overlays & Permissions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.Layers,
                    title = "Floating Metrics Overlay",
                    subtitle = "Show real-time FPS & usage on screen",
                    onClick = { 
                        if (Settings.canDrawOverlays(context)) {
                            // If running, stop it. If not, start it. For simplicity, just start/restart.
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
                    }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsClickableItem(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Notification Settings",
                    subtitle = "Manage alerts",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            })
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                )
            }
        }
        
        item { Text("Performance & Battery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Rounded.Speed,
                    title = "Accessibility Options",
                    subtitle = "Manage system access",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } catch (e: Exception) {}
                    }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsClickableItem(
                    icon = Icons.Rounded.BatterySaver,
                    title = "Extreme Power Saving",
                    subtitle = "Open Battery Saver Settings",
                    onClick = { 
                        try {
                            context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                        } catch (e: Exception) {}
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsClickableItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (actionIcon != null) {
                Icon(actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
