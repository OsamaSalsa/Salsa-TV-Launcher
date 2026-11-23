package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.TvMidnightBackground
import kotlin.math.PI
import kotlin.math.cos

data class AuroraColors(
    val glow: Color = Color(0xFF38BDF8),   // Vibrant Cyan/Sky Blue
    val mid: Color = Color(0xFF2563EB),    // Rich Royal Blue
    val accent: Color = Color(0xFF9333EA), // Vivid Purple/Violet
    val deep: Color = Color(0xFF1E1B4B),   // Deep Indigo/Navy
    val base: Color = Color(0xFF020617),   // Rich Obsidian Black
    val teal: Color = Color(0xFF0D9488),   // Luminous Teal
    val magenta: Color = Color(0xFFDB2777),// Cinematic Magenta Glow
    val amber: Color = Color(0xFFD97706)   // Subtle Warm Amber Highlight
)

fun Modifier.safeBlur(radius: Dp, edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle): Modifier {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasHardwareRenderNode) {
            this.blur(radius, edgeTreatment)
        } else {
            this
        }
    } catch (_: Throwable) {
        this
    }
}

/**
 * Resamples sparse gradient keyframes into pre-allocated C1-continuous cosine-eased tables
 * so Skia hardware shaders never produce Mach bands without allocating per animation frame.
 */
private fun fillCosineGradientStops(
    stops: FloatArray,
    colorsInput: Array<Color>,
    keyframeCount: Int,
    outColors: IntArray,
    outPositions: FloatArray
) {
    val steps = outColors.size
    val lastIdx = steps - 1
    for (i in 0 until steps) {
        val t = i.toFloat() / lastIdx.toFloat()
        outPositions[i] = t
        var segIdx = 0
        while (segIdx < keyframeCount - 2 && t > stops[segIdx + 1]) {
            segIdx++
        }
        val p0 = stops[segIdx]
        val p1 = stops[segIdx + 1]
        val span = (p1 - p0).coerceAtLeast(0.0001f)
        val localT = ((t - p0) / span).coerceIn(0f, 1f)
        val easedT = 0.5f * (1f - cos(PI.toFloat() * localT))
        outColors[i] = lerp(colorsInput[segIdx], colorsInput[segIdx + 1], easedT).toArgb()
    }
}

private object AuroraDitherCache {
    val paint: Paint by lazy {
        val size = 64
        val bmp = createBitmap(size, size)
        val rand = java.util.Random(1337L)
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            val bright = rand.nextBoolean()
            val lum = if (bright) 255 else 0
            val alpha = 1 + rand.nextInt(2)
            pixels[i] = android.graphics.Color.argb(alpha, lum, lum, lum)
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        Paint(Paint.DITHER_FLAG).apply {
            isDither = true
            isFilterBitmap = false
            shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }
}

private class AuroraOffscreenBuffer {
    val width = 256
    val height = 144
    val bitmap: Bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas: android.graphics.Canvas = android.graphics.Canvas(bitmap)
    val drawPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        isDither = true
        isFilterBitmap = true
    }
    val blitPaint: Paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
        isDither = true
        isFilterBitmap = true
    }
    val dstRect = android.graphics.RectF()
    var lastColorHash: Int = 0
    var hasRendered: Boolean = false
    var isRecycled: Boolean = false

    // Reusable scratch buffers so 280ms color transitions allocate zero heap memory per frame
    private val keyPositions = FloatArray(5)
    private val keyColors = Array(5) { Color.Transparent }
    private val cols12 = IntArray(12)
    private val pos12 = FloatArray(12)
    private val cols10 = IntArray(10)
    private val pos10 = FloatArray(10)

    private val topVignetteStops = floatArrayOf(0.00f, 0.45f, 1.00f)
    private val topVignetteColors = arrayOf(
        Color.Black.copy(alpha = 0.68f),
        Color.Black.copy(alpha = 0.24f),
        Color.Black.copy(alpha = 0f)
    )
    private val brVignetteStops = floatArrayOf(0.00f, 0.52f, 1.00f)
    private val brVignetteColors = arrayOf(
        Color.Black.copy(alpha = 0.86f),
        Color.Black.copy(alpha = 0.36f),
        Color.Black.copy(alpha = 0f)
    )
    private val topVignetteShader: LinearGradient by lazy {
        val c = IntArray(10)
        val p = FloatArray(10)
        fillCosineGradientStops(topVignetteStops, topVignetteColors, 3, c, p)
        LinearGradient(0f, 0f, 0f, height * 0.30f, c, p, Shader.TileMode.CLAMP)
    }
    private val brVignetteShader: RadialGradient by lazy {
        val c = IntArray(10)
        val p = FloatArray(10)
        fillCosineGradientStops(brVignetteStops, brVignetteColors, 3, c, p)
        RadialGradient(width * 1.05f, height * 1.05f, width * 0.68f, c, p, Shader.TileMode.CLAMP)
    }

    fun renderIfNeeded(
        glow: Color,
        teal: Color,
        mid: Color,
        accent: Color,
        deep: Color,
        base: Color,
        magenta: Color,
        amber: Color
    ) {
        if (isRecycled || bitmap.isRecycled) return
        var hash = glow.toArgb()
        hash = 31 * hash + teal.toArgb()
        hash = 31 * hash + mid.toArgb()
        hash = 31 * hash + accent.toArgb()
        hash = 31 * hash + deep.toArgb()
        hash = 31 * hash + base.toArgb()
        hash = 31 * hash + magenta.toArgb()
        hash = 31 * hash + amber.toArgb()
        if (hasRendered && hash == lastColorHash) return
        lastColorHash = hash
        hasRendered = true

        val w = width.toFloat()
        val h = height.toFloat()
        val nc = canvas

        nc.drawColor(base.toArgb())

        // 1) Seamless multi-stop diagonal backdrop gradient
        keyPositions[0] = 0.00f; keyPositions[1] = 0.38f; keyPositions[2] = 0.72f; keyPositions[3] = 1.00f
        keyColors[0] = deep.copy(alpha = 0.62f)
        keyColors[1] = accent.copy(alpha = 0.28f)
        keyColors[2] = base.copy(alpha = 0.78f)
        keyColors[3] = base
        fillCosineGradientStops(keyPositions, keyColors, 4, cols12, pos12)
        drawPaint.shader = LinearGradient(0f, 0f, w, h, cols12, pos12, Shader.TileMode.CLAMP)
        nc.drawRect(0f, 0f, w, h, drawPaint)

        // 2) Middle-left rich cobalt/sapphire bloom
        keyPositions[0] = 0.00f; keyPositions[1] = 0.45f; keyPositions[2] = 1.00f
        keyColors[0] = mid.copy(alpha = 0.78f)
        keyColors[1] = deep.copy(alpha = 0.36f)
        keyColors[2] = deep.copy(alpha = 0f)
        fillCosineGradientStops(keyPositions, keyColors, 3, cols10, pos10)
        val midCx = w * 0.22f
        val midCy = h * 0.42f
        val midR = w * 0.95f
        drawPaint.shader = RadialGradient(midCx, midCy, midR, cols10, pos10, Shader.TileMode.CLAMP)
        nc.drawCircle(midCx, midCy, midR, drawPaint)

        // 3) Upper-right luminous Teal & Cyan atmospheric beam
        keyPositions[0] = 0.00f; keyPositions[1] = 0.48f; keyPositions[2] = 1.00f
        keyColors[0] = teal.copy(alpha = 0.72f)
        keyColors[1] = glow.copy(alpha = 0.30f)
        keyColors[2] = teal.copy(alpha = 0f)
        fillCosineGradientStops(keyPositions, keyColors, 3, cols10, pos10)
        val tealCx = w * 0.75f
        val tealCy = h * 0.16f
        val tealR = w * 0.80f
        drawPaint.shader = RadialGradient(tealCx, tealCy, tealR, cols10, pos10, Shader.TileMode.CLAMP)
        nc.save()
        nc.scale(1.10f, 0.54f, tealCx, tealCy)
        nc.drawCircle(tealCx, tealCy, tealR, drawPaint)
        nc.restore()

        // 4) Top-center luminous Cyan hotspot band
        keyPositions[0] = 0.00f; keyPositions[1] = 0.24f; keyPositions[2] = 0.55f; keyPositions[3] = 0.80f; keyPositions[4] = 1.00f
        keyColors[0] = Color.White.copy(alpha = 0.32f)
        keyColors[1] = glow.copy(alpha = 0.64f)
        keyColors[2] = mid.copy(alpha = 0.34f)
        keyColors[3] = accent.copy(alpha = 0.12f)
        keyColors[4] = deep.copy(alpha = 0f)
        fillCosineGradientStops(keyPositions, keyColors, 5, cols12, pos12)
        val cyanCx = w * 0.35f
        val cyanCy = h * 0.12f
        val cyanR = w * 0.85f
        drawPaint.shader = RadialGradient(cyanCx, cyanCy, cyanR, cols12, pos12, Shader.TileMode.CLAMP)
        nc.save()
        nc.scale(1.05f, 0.48f, cyanCx, cyanCy)
        nc.drawCircle(cyanCx, cyanCy, cyanR, drawPaint)
        nc.restore()

        // 5) Right-side Magenta & Purple glow streak
        keyPositions[0] = 0.00f; keyPositions[1] = 0.50f; keyPositions[2] = 1.00f
        keyColors[0] = magenta.copy(alpha = 0.66f)
        keyColors[1] = accent.copy(alpha = 0.28f)
        keyColors[2] = magenta.copy(alpha = 0f)
        fillCosineGradientStops(keyPositions, keyColors, 3, cols10, pos10)
        val magCx = w * 0.82f
        val magCy = h * 0.36f
        val magR = w * 0.72f
        drawPaint.shader = RadialGradient(magCx, magCy, magR, cols10, pos10, Shader.TileMode.CLAMP)
        nc.save()
        nc.scale(1.20f, 0.48f, magCx, magCy)
        nc.drawCircle(magCx, magCy, magR, drawPaint)
        nc.restore()

        // 6) Bottom-left warm Amber/Gold subtle highlight
        keyPositions[0] = 0.00f; keyPositions[1] = 0.55f; keyPositions[2] = 1.00f
        keyColors[0] = amber.copy(alpha = 0.34f)
        keyColors[1] = accent.copy(alpha = 0.10f)
        keyColors[2] = accent.copy(alpha = 0f)
        fillCosineGradientStops(keyPositions, keyColors, 3, cols10, pos10)
        val ambCx = w * 0.15f
        val ambCy = h * 0.88f
        val ambR = w * 0.62f
        drawPaint.shader = RadialGradient(ambCx, ambCy, ambR, cols10, pos10, Shader.TileMode.CLAMP)
        nc.save()
        nc.scale(1.05f, 0.54f, ambCx, ambCy)
        nc.drawCircle(ambCx, ambCy, ambR, drawPaint)
        nc.restore()

        // 7) Smooth top cinematic vignette (cached shader)
        drawPaint.shader = topVignetteShader
        nc.drawRect(0f, 0f, w, h * 0.30f, drawPaint)

        // 8) Bottom-right corner rich vignette (cached shader)
        drawPaint.shader = brVignetteShader
        nc.drawCircle(w * 1.05f, h * 1.05f, w * 0.68f, drawPaint)
    }

    fun release() {
        isRecycled = true
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

@Composable
fun AuroraBackground(
    target: AuroraColors = AuroraColors(),
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    val spec = remember { tween<Color>(280) }
    val glowState = animateColorAsState(target.glow, spec, label = "glow")
    val tealState = animateColorAsState(target.teal, spec, label = "teal")
    val midState = animateColorAsState(target.mid, spec, label = "mid")
    val accentState = animateColorAsState(target.accent, spec, label = "accent")
    val deepState = animateColorAsState(target.deep, spec, label = "deep")
    val baseState = animateColorAsState(target.base, spec, label = "base")
    val magentaState = animateColorAsState(target.magenta, spec, label = "magenta")
    val amberState = animateColorAsState(target.amber, spec, label = "amber")

    val buffer = remember { AuroraOffscreenBuffer() }
    androidx.compose.runtime.DisposableEffect(buffer) {
        onDispose { buffer.release() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f || buffer.isRecycled || buffer.bitmap.isRecycled) return@Canvas

            buffer.renderIfNeeded(
                glowState.value,
                tealState.value,
                midState.value,
                accentState.value,
                deepState.value,
                baseState.value,
                magentaState.value,
                amberState.value
            )

            drawIntoCanvas { canvas ->
                val nc = canvas.nativeCanvas
                buffer.dstRect.set(0f, 0f, w, h)
                nc.drawBitmap(buffer.bitmap, null, buffer.dstRect, buffer.blitPaint)
                if (hasHardwareRenderNode) {
                    nc.drawRect(0f, 0f, w, h, AuroraDitherCache.paint)
                }
            }
        }

        content?.invoke()
    }
}

@Composable
fun AuroraBackground(
    colors: AuroraColors,
    modifier: Modifier = Modifier
) {
    AuroraBackground(target = colors, modifier = modifier, content = null)
}

// Extract colors from app icon using Palette & HSL shifts
fun auroraFromIcon(bitmap: Bitmap): AuroraColors {
    val p = Palette.from(bitmap).generate()
    val src = p.vibrantSwatch?.rgb
        ?: p.dominantSwatch?.rgb
        ?: p.lightVibrantSwatch?.rgb
        ?: p.darkVibrantSwatch?.rgb
        ?: p.mutedSwatch?.rgb
        ?: return AuroraColors()

    fun shift(h: Float, l: Float? = null, s: Float? = null): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(src, hsl)
        hsl[0] = (hsl[0] + h + 360f) % 360f
        l?.let { hsl[2] = it }
        s?.let { hsl[1] = it }
        return Color(ColorUtils.HSLToColor(hsl))
    }

    return AuroraColors(
        glow = shift(0f, l = 0.58f, s = 0.95f),
        mid = shift(-25f, l = 0.48f, s = 0.85f),
        accent = shift(70f, l = 0.40f, s = 0.75f),
        deep = shift(-10f, l = 0.22f, s = 0.80f),
        base = shift(0f, l = 0.02f, s = 0.40f),
        teal = shift(25f, l = 0.40f, s = 0.75f),
    )
}


@Composable
fun DynamicBackground(
    targetColorInt: Int? = null,
    auroraColors: AuroraColors = AuroraColors(),
    isEnabled: Boolean = true,
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    wallpaperVisibility: Int = 100,
    content: @Composable () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val clampedVisibility = wallpaperVisibility.coerceIn(10, 100)
    val dimAlphaState = animateFloatAsState(
        targetValue = (100 - clampedVisibility) / 100f,
        animationSpec = tween(240),
        label = "wallpaper_dim_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvMidnightBackground)
    ) {
        // Mirrored Background Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (isRtl) {
                        scaleX = -1f
                    }
                }
        ) {
            val hasValidCustomImage = (wallpaperMode == "custom" || wallpaperMode == "preset") &&
                customWallpaperUrl.isNotBlank() &&
                customWallpaperUrl != "aurora_gradient" &&
                !customWallpaperUrl.contains("1543946207")
            if (hasValidCustomImage) {
                val context = LocalContext.current
                val wallpaperRequest = remember(context, customWallpaperUrl) {
                    ImageRequest.Builder(context)
                        .data(customWallpaperUrl)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .allowRgb565(false)
                        .allowHardware(true)
                        .crossfade(200)
                        .build()
                }
                AsyncImage(
                    model = wallpaperRequest,
                    contentDescription = "Active Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (isEnabled) {
                AuroraBackground(colors = auroraColors)
            }
        }

        // Unified eye-comfort dimming layer drawn in draw phase (zero recomposition of content)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val alpha = dimAlphaState.value
                    if (alpha > 0.001f) {
                        drawRect(Color.Black.copy(alpha = alpha))
                    }
                }
        )

        // App Content Layer (Outside mirrored box)
        content()
    }
}

/**
 * Renders the user's primary background (Aurora gradient or active custom/preset wallpaper)
 * with the calm, dimmed ambient veil used in the Clock screensaver for eye comfort.
 */
@Composable
fun DimmedAmbientBackground(
    auroraColors: AuroraColors = AuroraColors(),
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    wallpaperVisibility: Int = 100,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DynamicBackground(
        auroraColors = auroraColors,
        isEnabled = true,
        wallpaperMode = wallpaperMode,
        customWallpaperUrl = customWallpaperUrl,
        wallpaperVisibility = wallpaperVisibility
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // Soft ambient glow orbs matching ClockDateScreensaver
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auroraColors.glow.copy(alpha = 0.20f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.32f),
                        radius = w * 0.36f
                    ),
                    center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.32f),
                    radius = w * 0.36f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auroraColors.accent.copy(alpha = 0.18f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.68f),
                        radius = w * 0.32f
                    ),
                    center = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.68f),
                    radius = w * 0.32f
                )
            }

            // Calm dimmed screensaver-clock veil for eye-friendly flat UI contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF030712).copy(alpha = 0.44f),
                                Color(0xFF020617).copy(alpha = 0.76f)
                            )
                        )
                    )
            )

            content()
        }
    }
}

