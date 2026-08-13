package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HudState(
    val fps: Int = 0,
    val cpu: Int = 0,
    val gpu: Int = 0,
    val temp: Float = 0f,
    val battery: Int = 0,
    val ping: Int = -1
)

@Composable
fun RogOverlayUI(state: HudState, onDrag: (Float, Float) -> Unit) {
    // ROG Theme Colors
    val neonRed = Color(0xFFFF0033)
    val darkGlass = Color(0xCC111111)
    
    // Blinking effect for thermal throttling
    val infiniteTransition = rememberInfiniteTransition(label = "thermal_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    // Temp color logic
    val (tempColor, isThrottling) = when {
        state.temp >= 44f -> Pair(neonRed, true)
        state.temp >= 39f -> Pair(Color.Yellow, false)
        else -> Pair(Color.Green, false)
    }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .background(darkGlass, RoundedCornerShape(50))
            .border(1.dp, neonRed, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricItem("FPS", "${state.fps}")
            MetricItem("CPU", "${state.cpu}%")
            MetricItem("GPU", "${state.gpu}%")
            
            // Temperature with blinking if throttling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alpha(if (isThrottling) blinkAlpha else 1f)
            ) {
                Text(
                    text = "TEMP",
                    color = tempColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.temp.toInt()}°C",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            
            MetricItem("BAT", "${state.battery}%")
            
            if (state.ping >= 0) {
                MetricItem("PING", "${state.ping}ms", if (state.ping < 80) Color.Green else if (state.ping < 150) Color.Yellow else neonRed)
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, labelColor: Color = Color(0xFFAAAAAA)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
