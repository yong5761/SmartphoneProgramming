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
    var barrierFramesLeft: Int = 0

    val barrierRadius get() = when (barrierType) {
        ItemType.SHIELD -> radius + 90f
        else -> radius + 30f
    }

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
            val base = it.color
            if (it == ItemType.SHIELD) {
                barrierFillPaint.color = Color.argb(70, Color.red(base), Color.green(base), Color.blue(base))
                canvas.drawCircle(x, y, barrierRadius, barrierFillPaint)
            }
            barrierStrokePaint.color = base
            canvas.drawCircle(x, y, barrierRadius, barrierStrokePaint)
        }
        canvas.drawCircle(x, y, radius, fillPaint)
        canvas.drawCircle(x, y, radius, strokePaint)
    }
}
