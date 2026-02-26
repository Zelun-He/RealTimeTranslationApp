package com.samanthamalca.translatorapp.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShazamScreenEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    // Limit the number of rings to 2
    val ripples = List(2) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1.2f,  // Expands beyond screen
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing), // Reduced duration
                repeatMode = RepeatMode.Restart
            ), label = "ripple$index"
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width * 0.5f  // Reduced from 0.8f to 0.5f

            ripples.forEach { ripple ->
                drawCircle(
                    color = Color.White.copy(alpha = 1f - ripple.value), // Fades out
                    radius = maxRadius * ripple.value, // Expanding
                    center = center
                )
            }
        }
    }
}
