package com.atelier.cards.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ivory = Color(0xFFF4EEDF)
val Gold = Color(0xFFBBA477)
val Felt = Color(0xFF101E1C)
val InkRed = Color(0xFF8E2937)
val InkBlue = Color(0xFF233F69)

@Composable
fun AtelierTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, onPrimary = Felt, background = Felt,
        surface = Color(0xFF1C2C28), onSurface = Ivory,
        secondary = Gold, onBackground = Ivory,
    ), content = content)
}
