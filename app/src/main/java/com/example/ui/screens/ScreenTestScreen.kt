package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun ScreenTestScreen(navController: NavController) {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.White, Color.Black, Color.Yellow, Color.Cyan, Color.Magenta
    )
    var currentColorIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[currentColorIndex])
            .clickable {
                if (currentColorIndex < colors.size - 1) {
                    currentColorIndex++
                } else {
                    navController.popBackStack()
                }
            }
    )
}
