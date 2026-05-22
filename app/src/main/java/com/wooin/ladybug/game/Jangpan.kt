package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Jangpan(
    val x: Float,
    val y: Float,
    val radius: Float,
    var framesLeft: Int
) {
    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, fillPaint)
        canvas.drawCircle(x, y, radius, strokePaint)
    }

    companion object {
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 0x2E, 0xCC, 0x71)
            style = Paint.Style.FILL
        }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2ECC71")
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
    }
}
