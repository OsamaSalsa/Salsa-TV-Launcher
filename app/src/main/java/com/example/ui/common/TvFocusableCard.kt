package com.example.ui.common

import android.view.KeyEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.theme.TvAccentGold
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FastFocusTween = tween<Float>(durationMillis = 110, easing = FastOutSlowInEasing)
private const val REMOTE_LONG_PRESS_MS = 380L

fun isRemoteMenuKey(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_MENU ||
        keyCode == KeyEvent.KEYCODE_TV_CONTENTS_MENU ||
        keyCode == KeyEvent.KEYCODE_TV_MEDIA_CONTEXT_MENU ||
        keyCode == KeyEvent.KEYCODE_SETTINGS ||
        keyCode == KeyEvent.KEYCODE_BOOKMARK ||
        keyCode == 82 ||
        keyCode == 174 ||
        keyCode == 176 ||
        keyCode == 256 ||
        keyCode == 257
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    shape: Shape = RoundedCornerShape(16.dp),
    scaleUpRatio: Float = 1.07f,
    elevation: Dp = 0.dp,
    cardBackgroundColor: Color? = null,
    useDefaultBorder: Boolean = true,
    focusBorderColor: Color = TvAccentGold,
    canFocus: Boolean = true,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var centerDownActive by remember { mutableStateOf(false) }
    var longClickTriggered by remember { mutableStateOf(false) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val interactionSource = remember { MutableInteractionSource() }

    val scaleState = animateFloatAsState(
        targetValue = if (isFocused) scaleUpRatio else 1.0f,
        animationSpec = FastFocusTween,
        label = "tv_card_scale"
    )

    val currentBackgroundColor = when {
        isFocused && cardBackgroundColor != null -> cardBackgroundColor
        isFocused -> Color.Transparent
        else -> cardBackgroundColor ?: Color.Transparent
    }

    Box(
        modifier = modifier
            .zIndex(if (isFocused) 2f else 0f)
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .onFocusChanged { focusState ->
                val nowFocused = focusState.isFocused
                if (isFocused != nowFocused) {
                    isFocused = nowFocused
                    if (!nowFocused) {
                        centerDownActive = false
                        longClickTriggered = false
                        longPressJob?.cancel()
                        longPressJob = null
                    } else {
                        onFocused?.invoke()
                    }
                }
            }
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (isFocused && currentOnLongClick != null && isRemoteMenuKey(native.keyCode)) {
                    if (native.action == KeyEvent.ACTION_DOWN) {
                        if (native.repeatCount == 0) {
                            currentOnLongClick?.invoke()
                        }
                        return@onKeyEvent true
                    } else if (native.action == KeyEvent.ACTION_UP) {
                        return@onKeyEvent true
                    }
                }
                if (isFocused) {
                    val isCenterOrEnter = native.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        native.keyCode == KeyEvent.KEYCODE_ENTER ||
                        native.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
                        native.keyCode == KeyEvent.KEYCODE_BUTTON_A
                    if (isCenterOrEnter) {
                        val longClickHandler = currentOnLongClick
                        if (longClickHandler != null) {
                            if (native.action == KeyEvent.ACTION_DOWN) {
                                if (native.repeatCount == 0 && !centerDownActive) {
                                    centerDownActive = true
                                    longClickTriggered = false
                                    longPressJob?.cancel()
                                    longPressJob = scope.launch {
                                        delay(REMOTE_LONG_PRESS_MS)
                                        if (centerDownActive && !longClickTriggered) {
                                            longClickTriggered = true
                                            currentOnLongClick?.invoke()
                                        }
                                    }
                                } else if (native.repeatCount >= 1 && centerDownActive && !longClickTriggered) {
                                    longClickTriggered = true
                                    longPressJob?.cancel()
                                    longPressJob = null
                                    longClickHandler()
                                }
                                // Always consume ACTION_DOWN when onLongClick is present so Compose's
                                // clickable never fires onClick prematurely on key-down!
                                return@onKeyEvent true
                            } else if (native.action == KeyEvent.ACTION_UP) {
                                longPressJob?.cancel()
                                longPressJob = null
                                val wasDown = centerDownActive
                                val wasLong = longClickTriggered
                                centerDownActive = false
                                longClickTriggered = false
                                if (wasDown && !wasLong) {
                                    currentOnClick()
                                }
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
            .focusRequester(focusRequester)
            .focusProperties {
                this.canFocus = canFocus
            }
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(currentBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        content(isFocused)
    }
}
