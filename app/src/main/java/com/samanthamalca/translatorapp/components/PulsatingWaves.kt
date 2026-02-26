package com.samanthamalca.translatorapp.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun PulsatingWaves() {
    var isPressed by remember { mutableStateOf(false) }

    val animationProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(320.dp)  // Increased from 200dp to accommodate larger button
            .clickable { isPressed = !isPressed }
    ) {
        Canvas(modifier = Modifier.size(320.dp)) {  // Increased canvas size
            val center = Offset(size.width / 2, size.height / 2)
            for (i in 1..4) {  // Added one more wave ring
                drawCircle(
                    color = Color(0xFF4A4AD3).copy(alpha = 0.35f - (i * 0.08f)),
                    radius = (size.minDimension / 2) * animationProgress * (i * 0.5f),
                    center = center
                )
            }
        }
        CircularDesign()
    }
}
