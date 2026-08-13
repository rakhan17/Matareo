package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.utils.CrosshairConfig

@Composable
fun CrosshairUI(config: CrosshairConfig) {
    val crosshairColor = Color(config.color)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = config.offsetX.dp, y = config.offsetY.dp)
            .scale(config.scale)
            .alpha(config.alpha),
        contentAlignment = Alignment.Center
    ) {
        if (config.imageUri.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(config.imageUri)),
                contentDescription = "Custom Crosshair",
                contentScale = ContentScale.Fit,
                modifier = Modifier.wrapContentSize()
            )
        } else {
            // Preset renderings
            when (config.preset) {
                1 -> { // Dot
                    Box(modifier = Modifier.size(8.dp).background(crosshairColor, CircleShape))
                }
                2 -> { // Cross
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = crosshairColor, modifier = Modifier.size(32.dp))
                }
                3 -> { // Circle with Dot
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, crosshairColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(4.dp).background(crosshairColor, CircleShape))
                    }
                }
                4 -> { // Hollow Circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, crosshairColor, CircleShape)
                    )
                }
                5 -> { // Neon Triangle Crosshair (Like image 1)
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        // Center Cross
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = crosshairColor, modifier = Modifier.size(24.dp))
                        // Outer Ring
                        Box(modifier = Modifier.size(48.dp).border(2.dp, crosshairColor.copy(alpha = 0.5f), CircleShape))
                        // Top Triangle
                        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-4).dp).size(12.dp).border(2.dp, crosshairColor))
                        // Bottom Triangle
                        Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp).size(12.dp).border(2.dp, crosshairColor))
                        // Left Triangle
                        Box(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-4).dp).size(12.dp).border(2.dp, crosshairColor))
                        // Right Triangle
                        Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 4.dp).size(12.dp).border(2.dp, crosshairColor))
                    }
                }
                6 -> { // Pixelated Square
                    Box(modifier = Modifier.size(24.dp).border(3.dp, crosshairColor), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(6.dp).background(crosshairColor))
                    }
                }
                7 -> { // Pixelated Double Square
                    Box(modifier = Modifier.size(32.dp).border(2.dp, crosshairColor), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(20.dp).border(2.dp, crosshairColor), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(6.dp).background(crosshairColor))
                        }
                    }
                }
                else -> {
                    // Default Dot
                    Box(modifier = Modifier.size(8.dp).background(crosshairColor, CircleShape))
                }
            }
        }
    }
}
