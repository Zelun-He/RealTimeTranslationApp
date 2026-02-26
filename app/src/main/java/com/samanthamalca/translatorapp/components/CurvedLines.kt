package com.samanthamalca.translatorapp.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Path

@Composable
fun CurvedLines() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Adjusted height to match the style
    ) {
        val width = size.width
        val height = size.height

        val path2 = Path().apply {
            moveTo(0f, height * 0.25f)
            cubicTo(width * 0.2f, height * 0.05f, width * 0.8f, height * 0.35f, width, height * 0.2f)
        }
        drawPath(
            path = path2,
            color = Color.White,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val path3 = Path().apply {
            moveTo(0f, height * 0.4f)
            cubicTo(width * 0.1f, height * 0.5f, width * 0.9f, height * 0.2f, width, height * 0.3f)
        }
        drawPath(
            path = path3,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        val path4 = Path().apply {
            moveTo(0f, height * 0.55f)
            cubicTo(width * 0.15f, height * 0.4f, width * 0.85f, height * 0.6f, width, height * 0.45f)
        }
        drawPath(
            path = path4,
            color = Color.White,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


