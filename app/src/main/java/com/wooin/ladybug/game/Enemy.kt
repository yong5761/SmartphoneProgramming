package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Enemy(
    var x: Float,
    var y: Float,
    val radius: Float,
    val vx: Float,
    val vy: Float
) {
    fun update(speedMultiplier: Float = 1f) {
        x += vx * speedMultiplier
        y += vy * speedMultiplier
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, fillPaint)
    }

    companion object {
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
    }
}
