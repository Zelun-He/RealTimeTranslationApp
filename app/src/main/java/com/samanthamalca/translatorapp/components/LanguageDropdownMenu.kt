package com.samanthamalca.translatorapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Language(val code: String, val name: String, val flag: String)

@Composable
fun LanguageDropdownMenu(
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(Language("en", "English", "🇺🇸")) }

    val languages = listOf(
        Language("en", "English", "🇺🇸"),
        Language("es", "Spanish", "🇪🇸"),
        Language("fr", "French", "🇫🇷"),
        Language("de", "German", "🇩🇪"),
        Language("it", "Italian", "🇮🇹"),
        Language("pt", "Portuguese", "🇵🇹"),
        Language("ru", "Russian", "🇷🇺"),
        Language("ja", "Japanese", "🇯🇵"),
        Language("zh", "Chinese", "🇨🇳"),
        Language("ko", "Korean", "🇰🇷"),
        Language("ar", "Arabic", "🇸🇦"),
        Language("hi", "Hindi", "🇮🇳")
    )

    // Animate rotation for hamburger icon
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 90f else 0f,
        animationSpec = tween(durationMillis = 300), label = "rotation"
    )

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        // Hamburger Menu Icon
        Canvas(
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
                .clickable {
                    isMenuOpen = !isMenuOpen
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

        // Dropdown Menu
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A2E).copy(alpha = 0.95f))
                    .padding(vertical = 8.dp)
            ) {
                // Current selection header
                Text(
                    text = "Select Language",
                    color = Color(0xFF93C5FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Language list
                languages.forEach { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language.code == selectedLanguage.code,
                        onClick = {
                            selectedLanguage = language
                            onLanguageSelected(language)
                            isMenuOpen = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language.flag,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = language.name,
            color = if (isSelected) Color.White else Color(0xFFE0E0E0),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


