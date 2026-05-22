package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Player(
    var x: Float,
    var y: Float,
    val radius: Float = 60f
) {

    var barrierColor: Int? = null

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

    private val barrierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    fun draw(canvas: Canvas) {
        barrierColor?.let {
            barrierPaint.color = it
            canvas.drawCircle(x, y, radius + 30f, barrierPaint)
        }
        canvas.drawCircle(x, y, radius, fillPaint)
        canvas.drawCircle(x, y, radius, strokePaint)
    }
}
