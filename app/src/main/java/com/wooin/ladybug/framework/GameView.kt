package com.wooin.ladybug.framework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.wooin.ladybug.R
import com.wooin.ladybug.game.Player
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.bg_grass)
    private val viewRect = Rect()
    private var player: Player? = null

    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var isTouching: Boolean = false
    private var loopRunning: Boolean = false

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val p = player
            if (p != null && isTouching) {
                movePlayerTowardTarget(p)
                invalidate()
                choreographer.postFrameCallback(this)
            } else {
                loopRunning = false
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(0, 0, w, h)
        if (player == null) {
            player = Player(x = w / 2f, y = h * 0.75f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(backgroundBitmap, null, viewRect, null)
        player?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (player == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                targetX = event.x
                targetY = event.y
                isTouching = true
                startLoopIfNeeded()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        choreographer.removeFrameCallback(frameCallback)
        loopRunning = false
        isTouching = false
    }

    private fun startLoopIfNeeded() {
        if (!loopRunning) {
            loopRunning = true
            choreographer.postFrameCallback(frameCallback)
        }
    }

    private fun movePlayerTowardTarget(p: Player) {
        val dx = targetX - p.x
        val dy = targetY - p.y
        val dist = sqrt(dx * dx + dy * dy)
        val minX = p.radius
        val minY = p.radius
        val maxX = width - p.radius
        val maxY = height - p.radius
        if (dist <= PLAYER_SPEED) {
            p.x = targetX.coerceIn(minX, maxX)
            p.y = targetY.coerceIn(minY, maxY)
        } else {
            val nx = dx / dist
            val ny = dy / dist
            p.x = (p.x + nx * PLAYER_SPEED).coerceIn(minX, maxX)
            p.y = (p.y + ny * PLAYER_SPEED).coerceIn(minY, maxY)
        }
    }

    companion object {
        private const val PLAYER_SPEED = 8f
    }
}
