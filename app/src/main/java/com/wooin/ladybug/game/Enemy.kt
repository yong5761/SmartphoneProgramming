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
        canvas.drawCircle(x, y, radius, bodyPaint)
        canvas.drawCircle(x, y, radius, outlinePaint)

        val ex = radius * 0.28f
        val ey = y - radius * 0.12f
        val er = radius * 0.28f

        canvas.drawCircle(x - ex, ey, er, eyeWhitePaint)
        canvas.drawCircle(x + ex, ey, er, eyeWhitePaint)

        val pr = er * 0.52f
        canvas.drawCircle(x - ex + pr * 0.15f, ey + pr * 0.15f, pr, pupilPaint)
        canvas.drawCircle(x + ex - pr * 0.15f, ey + pr * 0.15f, pr, pupilPaint)

        canvas.drawCircle(x - ex, ey, er, eyeOutlinePaint)
        canvas.drawCircle(x + ex, ey, er, eyeOutlinePaint)
    }

    companion object {
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D32F2F")
            style = Paint.Style.FILL
        }
        private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7B0000")
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        private val eyeOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            style = Paint.Style.FILL
        }
    }
}
