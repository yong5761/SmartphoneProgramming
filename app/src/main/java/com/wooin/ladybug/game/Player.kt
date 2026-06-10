package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.atan2
import kotlin.math.PI

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
    var hasLifeBarrier: Boolean = false

    val barrierRadius get() = when (barrierType) {
        ItemType.SHIELD -> radius + 90f
        else -> radius + 30f
    }
    val lifeBarrierRadius = radius + 30f

    private val barrierFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val barrierStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 8f }
    private val lifeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 8f; color = ItemType.LIFE.color
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

    fun draw(canvas: Canvas, targetX: Float = x, targetY: Float = y - 100f) {
        if (hasLifeBarrier) canvas.drawCircle(x, y, lifeBarrierRadius, lifeStrokePaint)
        barrierType?.let {
            if (it == ItemType.SHIELD) canvas.drawCircle(x, y, barrierRadius, barrierFillPaint)
            canvas.drawCircle(x, y, barrierRadius, barrierStrokePaint)
        }

        val angle = (atan2((targetY - y).toDouble(), (targetX - x).toDouble()) * 180.0 / PI + 90.0).toFloat()
        canvas.save()
        canvas.rotate(angle, x, y)

        canvas.drawCircle(x, y, radius, bodyPaint)
        canvas.drawLine(x, y - radius * 0.3f, x, y + radius * 0.95f, dividerPaint)

        val sr = radius * 0.17f
        canvas.drawCircle(x - radius * 0.33f, y - radius * 0.1f, sr, spotPaint)
        canvas.drawCircle(x + radius * 0.33f, y - radius * 0.1f, sr, spotPaint)
        canvas.drawCircle(x - radius * 0.28f, y + radius * 0.38f, sr, spotPaint)
        canvas.drawCircle(x + radius * 0.28f, y + radius * 0.38f, sr, spotPaint)

        canvas.drawCircle(x, y, radius, bodyOutlinePaint)

        val ex = radius * 0.3f
        val ey = y - radius * 0.62f
        val er = radius * 0.3f
        canvas.drawCircle(x - ex, ey, er, eyeWhitePaint)
        canvas.drawCircle(x + ex, ey, er, eyeWhitePaint)

        val pr = radius * 0.16f
        canvas.drawCircle(x - ex, ey, pr, pupilPaint)
        canvas.drawCircle(x + ex, ey, pr, pupilPaint)

        val hr = radius * 0.07f
        canvas.drawCircle(x - ex + pr * 0.35f, ey - pr * 0.35f, hr, highlightPaint)
        canvas.drawCircle(x + ex + pr * 0.35f, ey - pr * 0.35f, hr, highlightPaint)

        canvas.drawCircle(x - ex, ey, er, eyeOutlinePaint)
        canvas.drawCircle(x + ex, ey, er, eyeOutlinePaint)

        canvas.restore()
    }

    companion object {
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B8D400"); style = Paint.Style.FILL
        }
        private val bodyOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333"); style = Paint.Style.STROKE; strokeWidth = 5f
        }
        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333"); style = Paint.Style.STROKE; strokeWidth = 4f
        }
        private val spotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A"); style = Paint.Style.FILL
        }
        private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; style = Paint.Style.FILL
        }
        private val eyeOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333"); style = Paint.Style.STROKE; strokeWidth = 4f
        }
        private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111"); style = Paint.Style.FILL
        }
        private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; style = Paint.Style.FILL
        }
    }
}
