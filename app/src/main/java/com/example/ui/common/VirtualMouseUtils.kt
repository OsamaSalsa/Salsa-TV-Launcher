package com.example.ui.common

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import kotlin.math.min
import kotlin.math.roundToInt

internal enum class VirtualMouseDirection {
    LEFT, RIGHT, UP, DOWN
}

internal data class VirtualMouseStepResult(
    val nextX: Float,
    val nextY: Float,
    val scrollDx: Int = 0,
    val scrollDy: Int = 0,
    val reachedEdgeBeyondLimit: Boolean = false
)

internal fun resolveVirtualMouseStepPx(
    viewportWidth: Float,
    viewportHeight: Float,
    repeatCount: Int = 0
): Float {
    val base = min(viewportWidth.coerceAtLeast(1f), viewportHeight.coerceAtLeast(1f))
    val baseStep = (base * 0.032f).coerceIn(20f, 44f)
    val speedMultiplier = when {
        repeatCount >= 8 -> 1.85f
        repeatCount >= 3 -> 1.35f
        else -> 1.0f
    }
    return baseStep * speedMultiplier
}

internal fun resolveVirtualMouseScrollStepPx(viewportHeight: Float): Int {
    return (viewportHeight.coerceAtLeast(1f) * 0.22f).roundToInt().coerceIn(140, 300)
}

internal fun clampVirtualMousePosition(
    x: Float,
    y: Float,
    viewportWidth: Float,
    viewportHeight: Float
): Pair<Float, Float> {
    val maxX = (viewportWidth - 2f).coerceAtLeast(0f)
    val maxY = (viewportHeight - 2f).coerceAtLeast(0f)
    return x.coerceIn(0f, maxX) to y.coerceIn(0f, maxY)
}

/**
 * Computes one D-pad step for the virtual mouse cursor.
 * Allows the cursor to travel all the way to every viewport edge (0..maxX, 0..maxY)
 * without artificial dead zones, and only triggers WebView scrolling once the cursor
 * has reached the corresponding edge.
 */
internal fun stepVirtualMouse(
    currentX: Float,
    currentY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    direction: VirtualMouseDirection,
    stepPx: Float = resolveVirtualMouseStepPx(viewportWidth, viewportHeight),
    scrollStepPx: Int = resolveVirtualMouseScrollStepPx(viewportHeight),
    canScrollInDirection: Boolean = false
): VirtualMouseStepResult {
    val maxX = (viewportWidth - 2f).coerceAtLeast(0f)
    val maxY = (viewportHeight - 2f).coerceAtLeast(0f)
    val clampedX = currentX.coerceIn(0f, maxX)
    val clampedY = currentY.coerceIn(0f, maxY)
    val edgeEpsilon = 0.5f

    return when (direction) {
        VirtualMouseDirection.LEFT -> {
            if (clampedX > edgeEpsilon) {
                VirtualMouseStepResult(
                    nextX = (clampedX - stepPx).coerceAtLeast(0f),
                    nextY = clampedY
                )
            } else {
                VirtualMouseStepResult(
                    nextX = 0f,
                    nextY = clampedY,
                    scrollDx = if (canScrollInDirection) -scrollStepPx else 0,
                    reachedEdgeBeyondLimit = !canScrollInDirection
                )
            }
        }
        VirtualMouseDirection.RIGHT -> {
            if (clampedX < maxX - edgeEpsilon) {
                VirtualMouseStepResult(
                    nextX = (clampedX + stepPx).coerceAtMost(maxX),
                    nextY = clampedY
                )
            } else {
                VirtualMouseStepResult(
                    nextX = maxX,
                    nextY = clampedY,
                    scrollDx = if (canScrollInDirection) scrollStepPx else 0,
                    reachedEdgeBeyondLimit = !canScrollInDirection
                )
            }
        }
        VirtualMouseDirection.UP -> {
            if (clampedY > edgeEpsilon) {
                VirtualMouseStepResult(
                    nextX = clampedX,
                    nextY = (clampedY - stepPx).coerceAtLeast(0f)
                )
            } else {
                VirtualMouseStepResult(
                    nextX = clampedX,
                    nextY = 0f,
                    scrollDy = -scrollStepPx,
                    reachedEdgeBeyondLimit = !canScrollInDirection
                )
            }
        }
        VirtualMouseDirection.DOWN -> {
            if (clampedY < maxY - edgeEpsilon) {
                VirtualMouseStepResult(
                    nextX = clampedX,
                    nextY = (clampedY + stepPx).coerceAtMost(maxY)
                )
            } else {
                VirtualMouseStepResult(
                    nextX = clampedX,
                    nextY = maxY,
                    scrollDy = scrollStepPx,
                    reachedEdgeBeyondLimit = !canScrollInDirection
                )
            }
        }
    }
}

internal fun dispatchVirtualMouseHover(
    targetView: View?,
    x: Float,
    y: Float
) {
    if (targetView == null) return
    if (targetView is WebView && isWebViewDestroyed(targetView)) return
    val maxX = (targetView.width.takeIf { it > 0 }?.toFloat()?.minus(1f) ?: x).coerceAtLeast(0f)
    val maxY = (targetView.height.takeIf { it > 0 }?.toFloat()?.minus(1f) ?: y).coerceAtLeast(0f)
    val touchX = x.coerceIn(0f, maxX)
    val touchY = y.coerceIn(0f, maxY)
    val now = SystemClock.uptimeMillis()
    val hoverEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_HOVER_MOVE, touchX, touchY, 0).apply {
        source = InputDevice.SOURCE_MOUSE
    }
    try {
        targetView.dispatchGenericMotionEvent(hoverEvent)
    } catch (_: Exception) {
    } finally {
        hoverEvent.recycle()
    }
}

internal fun dispatchVirtualMouseClick(
    targetView: View?,
    x: Float,
    y: Float,
    onFeedbackComplete: () -> Unit
): Boolean {
    if (targetView == null) return false
    if (targetView is WebView && isWebViewDestroyed(targetView)) return false
    val maxX = (targetView.width.takeIf { it > 0 }?.toFloat()?.minus(1f) ?: x).coerceAtLeast(0f)
    val maxY = (targetView.height.takeIf { it > 0 }?.toFloat()?.minus(1f) ?: y).coerceAtLeast(0f)
    val touchX = x.coerceIn(0f, maxX)
    val touchY = y.coerceIn(0f, maxY)
    val downTime = SystemClock.uptimeMillis()
    val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, touchX, touchY, 0)
    val upEvent = MotionEvent.obtain(downTime, downTime + 45L, MotionEvent.ACTION_UP, touchX, touchY, 0)
    try {
        targetView.dispatchTouchEvent(downEvent)
        targetView.dispatchTouchEvent(upEvent)
    } catch (_: Exception) {
    } finally {
        downEvent.recycle()
        upEvent.recycle()
    }
    postOnMain(120L) { onFeedbackComplete() }
    return true
}
