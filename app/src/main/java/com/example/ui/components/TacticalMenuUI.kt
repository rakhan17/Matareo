package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.TacticalConfig
import com.example.utils.TacticalPrefs

val CyanNeon = Color(0xFF00F0FF)
val CrimsonRed = Color(0xFFFF003C)

@Composable
fun TacticalMenuUI(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { TacticalPrefs.getInstance(context) }
    val config by prefs.configFlow.collectAsState()

    Box(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentSize(Alignment.TopStart)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Floating Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xCC050505), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(CyanNeon, CrimsonRed)),
                        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .clickable { onToggleExpand() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Adb, contentDescription = "Menu", tint = CyanNeon, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Menu
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .background(Color(0xDD050505), CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(CrimsonRed, CyanNeon)),
                            shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "TACTICAL MENU",
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                    
                    Divider(color = Color(0x33FFFFFF))

                    CyberToggle(
                        label = "RAPID FIRE MACRO",
                        isChecked = config.macroEnabled,
                        onCheckedChange = { prefs.updateConfig(config.copy(macroEnabled = it)) }
                    )

                    CyberToggle(
                        label = "SHADOW PIERCER",
                        isChecked = config.shadowPiercerEnabled,
                        onCheckedChange = { prefs.updateConfig(config.copy(shadowPiercerEnabled = it)) }
                    )
                    
                    CyberToggle(
                        label = "SENSITIVITY OD",
                        isChecked = config.sensitivityOverdriveEnabled,
                        onCheckedChange = { prefs.updateConfig(config.copy(sensitivityOverdriveEnabled = it)) }
                    )

                    CyberToggle(
                        label = "SNIPER SCOPE",
                        isChecked = config.sniperScopeEnabled,
                        onCheckedChange = { prefs.updateConfig(config.copy(sniperScopeEnabled = it)) }
                    )
                }
            }
        }
    }
}

@Composable
fun CyberToggle(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            color = if (isChecked) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 20.dp)
                .background(
                    if (isChecked) CrimsonRed.copy(alpha = 0.2f) else Color(0x33FFFFFF),
                    CutCornerShape(4.dp)
                )
                .border(
                    1.dp,
                    if (isChecked) CrimsonRed else Color.DarkGray,
                    CutCornerShape(4.dp)
                )
                .padding(2.dp),
            contentAlignment = if (isChecked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (isChecked) CyanNeon else Color.Gray, CutCornerShape(2.dp))
            )
        }
    }
}
