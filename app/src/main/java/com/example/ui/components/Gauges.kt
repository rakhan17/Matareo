package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Speedometer(
    currentValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val progress = (currentValue / maxValue).coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(2f)) {
        val sweepAngle = 180f
        val strokeWidth = 24.dp.toPx()
        val size = Size(size.width, size.width)
        val topLeft = Offset(0f, (size.height - size.width) / 2)

        // Draw track
        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw progress
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw needle
        val center = Offset(size.width / 2, size.width / 2)
        val needleAngle = 180f + (sweepAngle * progress)
        val needleLength = size.width / 2f - strokeWidth * 1.5f

        rotate(needleAngle, center) {
            drawLine(
                color = color,
                start = center,
                end = Offset(center.x + needleLength, center.y),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color,
                radius = 12.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
fun CircularGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 16.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val inset = strokeWidth / 2f
        
        drawCircle(
            color = trackColor,
            radius = diameter / 2f,
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
