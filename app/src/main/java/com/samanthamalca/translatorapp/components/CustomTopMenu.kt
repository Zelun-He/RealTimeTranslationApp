package com.samanthamalca.translatorapp.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun CustomTopMenu(onClick: () -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }

    // Animate rotation for the "X" effect
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 45f else 0f,
        animationSpec = tween(durationMillis = 300), label = "rotation"
    )

    Canvas(
        modifier = Modifier
            .size(50.dp)
            .padding(8.dp)
            .clickable {
                isMenuOpen = !isMenuOpen
                onClick()
            }
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        val lineWidth = size.width * 0.9f
        val lineSpacing = size.height / 4
        val totalHeight = (2 * lineSpacing) + (4.dp.toPx() * 2)
        val startY = (size.height - totalHeight) / 2

        for (i in 0..2) {
            val y = startY + i * lineSpacing
            drawLine(
                color = Color.White,
                start = Offset(0f, y),
                end = Offset(lineWidth, y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
