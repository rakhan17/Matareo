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
    Box(
        modifier = Modifier
            .offset(x = config.offsetX.dp, y = config.offsetY.dp)
            .scale(config.scale)
            .alpha(config.alpha)
            .size(100.dp), // arbitrary bounds for centering
        contentAlignment = Alignment.Center
    ) {
        if (config.imageUri.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(config.imageUri)),
                contentDescription = "Custom Crosshair",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp)
            )
        } else {
            // Preset renderings
            when (config.preset) {
                1 -> { // Dot
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                }
                2 -> { // Cross
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.Green, modifier = Modifier.size(32.dp))
                }
                3 -> { // Circle with Dot
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(4.dp).background(Color.Red, CircleShape))
                    }
                }
                4 -> { // Hollow Circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, Color.Green, CircleShape)
                    )
                }
                5 -> { // Blue Cross
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(32.dp))
                }
                else -> {
                    // Default Dot
                    Box(modifier = Modifier.size(8.dp).background(Color.Yellow, CircleShape))
                }
            }
        }
    }
}
