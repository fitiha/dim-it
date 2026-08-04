package com.screendimmer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

class DimmerOverlay(context: Context) : View(context) {

    private var dimAlpha: Float = 0.5f

    fun setDimLevel(level: Int) {
        dimAlpha = level.coerceIn(0, 100) / 100f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.argb((dimAlpha * 255).toInt(), 0, 0, 0))
    }
}
