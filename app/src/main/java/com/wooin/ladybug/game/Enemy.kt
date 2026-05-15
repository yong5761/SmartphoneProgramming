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

    private val fillPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun update() {
        x += vx
        y += vy
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, fillPaint)
    }
}
