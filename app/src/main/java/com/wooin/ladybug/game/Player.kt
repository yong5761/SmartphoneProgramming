package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Player(
    var x: Float,
    var y: Float,
    val radius: Float = 60f
) {
    var barrierType: ItemType? = null
        set(value) {
            field = value
            if (value != null) {
                val base = value.color
                barrierStrokePaint.color = base
                barrierFillPaint.color = Color.argb(70, Color.red(base), Color.green(base), Color.blue(base))
            }
        }

    var barrierFramesLeft: Int = 0

    val barrierRadius get() = when (barrierType) {
        ItemType.SHIELD -> radius + 90f
        else -> radius + 30f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val barrierFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val barrierStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    fun tickBarrier() {
        if (barrierType != null) {
            barrierFramesLeft--
            if (barrierFramesLeft <= 0) {
                barrierType = null
                barrierFramesLeft = 0
            }
        }
    }

    fun draw(canvas: Canvas) {
        barrierType?.let {
            if (it == ItemType.SHIELD) canvas.drawCircle(x, y, barrierRadius, barrierFillPaint)
            canvas.drawCircle(x, y, barrierRadius, barrierStrokePaint)
        }
        canvas.drawCircle(x, y, radius, fillPaint)
        canvas.drawCircle(x, y, radius, strokePaint)
    }
}
