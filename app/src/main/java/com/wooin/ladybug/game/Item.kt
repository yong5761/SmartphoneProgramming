package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

enum class ItemType(val color: Int) {
    JANGPAN(Color.parseColor("#2ECC71")),
    SHIELD(Color.parseColor("#3498DB")),
    SLOW(Color.parseColor("#9B59B6")),
    HEAL(Color.parseColor("#E91E63")),
    SCORE(Color.parseColor("#F1C40F"))
}

class Item(
    var x: Float,
    var y: Float,
    val radius: Float,
    val vx: Float,
    val vy: Float,
    val type: ItemType
) {

    private val fillPaint = Paint().apply {
        color = type.color
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
