package com.screendimmer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

class DimmerOverlay(context: Context) : View(context) {

    private var dimAlpha: Float = 0.5f
    private var statusBarHeight: Int = 0
    private var navBarHeight: Int = 0

    fun setDimLevel(level: Int) {
        dimAlpha = level.coerceIn(0, 100) / 100f
        invalidate()
    }

    fun setSystemBars(statusBar: Int, navBar: Int) {
        statusBarHeight = statusBar
        navBarHeight = navBar
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val color = Color.argb((dimAlpha * 255).toInt(), 0, 0, 0)
        canvas.save()
        canvas.clipRect(0f, statusBarHeight.toFloat(), width.toFloat(), (height - navBarHeight).toFloat())
        canvas.drawColor(color)
        canvas.restore()
    }
}
