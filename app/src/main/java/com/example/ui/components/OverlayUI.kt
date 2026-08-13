package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class HudState(
    val fps: Int = 0,
    val cpu: Int = 0,
    val gpu: Int = 0,
    val temp: Float = 0f,
    val battery: Int = 0,
    val ping: Int = 0
)

@Composable
fun OverlayUI(state: HudState, onDrag: (Float, Float) -> Unit) {
    var isMinimized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        if (isMinimized) {
            // Minimized State: Just 32x32 App Logo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xCC000000))
                    .clickable { isMinimized = false },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Restore HUD",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Full UI: Ultra-thin
            Row(
                modifier = Modifier
                    .heightIn(max = 32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Drag handle icon
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Drag",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )

                // FPS
                MetricItem(icon = Icons.Rounded.Speed, value = "${state.fps}", color = Color(0xFF66FCF1))
                
                // CPU
                MetricItem(icon = Icons.Rounded.Memory, value = "${state.cpu}%", color = Color.White)
                
                // GPU
                MetricItem(icon = Icons.Rounded.GraphicEq, value = "${state.gpu}%", color = Color.White)
                
                // Temp
                val tempColor = if (state.temp > 42f) Color.Red else Color.White
                MetricItem(icon = Icons.Rounded.Thermostat, value = "${state.temp}°C", color = tempColor)
                
                // Battery
                MetricItem(icon = Icons.Rounded.BatteryFull, value = "${state.battery}%", color = Color.Green)
                
                // Ping
                MetricItem(icon = Icons.Rounded.NetworkPing, value = "${state.ping}ms", color = Color.White)

                // Minimize Button
                Icon(
                    imageVector = Icons.Rounded.CloseFullscreen,
                    contentDescription = "Minimize",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { isMinimized = true }
                )
            }
        }
    }
}

@Composable
fun MetricItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
