package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Player(
    var x: Float,
    var y: Float,
    val radius: Float = 60f
) {

    private val fillPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, fillPaint)
        canvas.drawCircle(x, y, radius, strokePaint)
    }
}
