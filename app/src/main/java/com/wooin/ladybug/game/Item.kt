package com.wooin.ladybug.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

enum class ItemType(val color: Int) {
    JANGPAN(Color.parseColor("#2ECC71")),
    SHIELD(Color.parseColor("#3498DB")),
    SLOW(Color.parseColor("#9B59B6")),
    LIFE(Color.parseColor("#E91E63")),
    SCORE(Color.parseColor("#F1C40F"))
}

class Item(
    var x: Float,
    var y: Float,
    val radius: Float,
    var vx: Float,
    var vy: Float,
    val type: ItemType
) {
    fun update(viewWidth: Int, viewHeight: Int) {
        x += vx
        y += vy
        if (x < radius && vx < 0) {
            x = radius
            vx = -vx
        } else if (x > viewWidth - radius && vx > 0) {
            x = viewWidth - radius
            vx = -vx
        }
        if (y < radius && vy < 0) {
            y = radius
            vy = -vy
        } else if (y > viewHeight - radius && vy > 0) {
            y = viewHeight - radius
            vy = -vy
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, paints[type.ordinal])
    }

    companion object {
        private val paints = ItemType.entries.map { t ->
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = t.color; style = Paint.Style.FILL }
        }.toTypedArray()
    }
}
