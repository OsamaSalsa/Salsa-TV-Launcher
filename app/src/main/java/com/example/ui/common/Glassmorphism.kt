package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TvAccentGold

/**
 * Flat, eye-comfortable overlay for TV cards:
 * - Unfocused: completely borderless and flat (no prominent strokes or bright edge lines).
 * - Focused: subtle tonal lift with a whisper-soft, non-intrusive edge tint.
 */
@Composable
fun GlassSheenOverlay(
    isFocused: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    glassShadowAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (!isFocused) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
    )
}
