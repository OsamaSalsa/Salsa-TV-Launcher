package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap

fun drawableToBitmapSafe(drawable: Drawable): Bitmap {
    return try {
        val raw = if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            drawable.toBitmap(width = 128, height = 128, config = Bitmap.Config.ARGB_8888)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && raw.config == Bitmap.Config.HARDWARE) {
            raw.copy(Bitmap.Config.ARGB_8888, false) ?: createBitmap(128, 128)
        } else {
            raw
        }
    } catch (_: Exception) {
        createBitmap(128, 128)
    }
}
