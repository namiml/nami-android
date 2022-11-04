package com.namiml.app.test.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProductionColorPalette = darkColors(
    primary = Yellow,
    background = Color.White,
    onBackground = Smoke,
    surface = Navy
)

private val StagingColorPalette = darkColors(
    primary = PrimaryBlue,
    background = Color.White,
    onBackground = Smoke,
    surface = Navy,
)

@Composable
fun TestNamiTheme(production: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (production) {
        ProductionColorPalette
    } else {
        StagingColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}