package com.wooin.ladybug.framework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.wooin.ladybug.R
import com.wooin.ladybug.game.Enemy
import com.wooin.ladybug.game.Item
import com.wooin.ladybug.game.ItemType
import com.wooin.ladybug.game.Player
import kotlin.math.sqrt
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val backgroundBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.bg_grass)
    private val viewRect = Rect()
    private var player: Player? = null
    private val enemies = mutableListOf<Enemy>()
    private val items = mutableListOf<Item>()
    private var framesSinceSpawn = 0
    private var framesSinceItemSpawn = 0

    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var isTouching: Boolean = false
    private var loopRunning: Boolean = false
    private var isGameOver: Boolean = false

    private val restartButtonRect = RectF()

    private val gameOverTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f * density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(10f, 4f, 4f, Color.BLACK)
    }

    private val restartButtonFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E74C3C")
        style = Paint.Style.FILL
    }

    private val restartButtonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f * density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val p = player
            if (p == null) {
                loopRunning = false
                return
            }
            if (isTouching) movePlayerTowardTarget(p)
            updateEnemies()
            updateItems()
            spawnEnemyIfDue()
            spawnItemIfDue()
            if (checkCollision(p)) isGameOver = true
            invalidate()
            if (isGameOver) {
                loopRunning = false
            } else {
                choreographer.postFrameCallback(this)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(0, 0, w, h)
        layoutRestartButton(w, h)
        fitGameOverTextSize(w)
        if (player == null) {
            player = Player(x = w / 2f, y = h * 0.75f)
            startLoopIfNeeded()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(backgroundBitmap, null, viewRect, null)
        enemies.forEach { it.draw(canvas) }
        items.forEach { it.draw(canvas) }
        player?.draw(canvas)
        if (isGameOver) {
            drawGameOverOverlay(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (player == null) return false
        if (isGameOver) {
            if (event.action == MotionEvent.ACTION_DOWN &&
                restartButtonRect.contains(event.x, event.y)
            ) {
                restartGame()
            }
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                targetX = event.x
                targetY = event.y
                isTouching = true
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

    private fun updateEnemies() {
        val it = enemies.iterator()
        while (it.hasNext()) {
            val e = it.next()
            e.update()
            if (e.y - e.radius > height) it.remove()
        }
    }

    private fun updateItems() {
        items.forEach { it.update(width, height) }
    }

    private fun spawnEnemyIfDue() {
        framesSinceSpawn++
        if (framesSinceSpawn < SPAWN_INTERVAL_FRAMES) return
        framesSinceSpawn = 0
        if (width <= 0) return
        val r = ENEMY_RADIUS
        val x = Random.nextFloat() * (width - 2 * r) + r
        val y = -r
        val vx = Random.nextFloat() * 6f - 3f
        val vy = Random.nextFloat() * 4f + 3f
        enemies.add(Enemy(x = x, y = y, radius = r, vx = vx, vy = vy))
    }

    private fun spawnItemIfDue() {
        framesSinceItemSpawn++
        if (framesSinceItemSpawn < ITEM_SPAWN_INTERVAL_FRAMES) return
        if (items.size >= MAX_ITEMS_ON_SCREEN) return
        framesSinceItemSpawn = 0
        if (width <= 0) return
        val r = ENEMY_RADIUS
        val x = Random.nextFloat() * (width - 2 * r) + r
        val y = -r
        val vx = Random.nextFloat() * 6f - 3f
        val vy = Random.nextFloat() * 4f + 3f
        val types = ItemType.values()
        val type = types[Random.nextInt(types.size)]
        items.add(Item(x = x, y = y, radius = r, vx = vx, vy = vy, type = type))
    }

    private fun checkCollision(p: Player): Boolean {
        for (e in enemies) {
            val dx = p.x - e.x
            val dy = p.y - e.y
            val rsum = p.radius + e.radius
            if (dx * dx + dy * dy <= rsum * rsum) return true
        }
        return false
    }

    private fun fitGameOverTextSize(w: Int) {
        val targetWidth = w * 0.85f
        gameOverTextPaint.textSize = 64f * density
        val measured = gameOverTextPaint.measureText("GAME OVER")
        if (measured > targetWidth) {
            gameOverTextPaint.textSize *= targetWidth / measured
        }
    }

    private fun layoutRestartButton(w: Int, h: Int) {
        val btnW = 240f * density
        val btnH = 80f * density
        val cx = w / 2f
        val cy = h / 2f + 60f * density
        restartButtonRect.set(cx - btnW / 2, cy - btnH / 2, cx + btnW / 2, cy + btnH / 2)
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(160, 0, 0, 0))
        val gameOverY = height / 2f - 60f * density
        canvas.drawText("GAME OVER", width / 2f, gameOverY, gameOverTextPaint)
        val cornerRadius = 20f * density
        canvas.drawRoundRect(restartButtonRect, cornerRadius, cornerRadius, restartButtonFillPaint)
        val textY = restartButtonRect.centerY() -
            (restartButtonTextPaint.descent() + restartButtonTextPaint.ascent()) / 2
        canvas.drawText("다시?", restartButtonRect.centerX(), textY, restartButtonTextPaint)
    }

    private fun restartGame() {
        isGameOver = false
        isTouching = false
        enemies.clear()
        items.clear()
        framesSinceSpawn = 0
        framesSinceItemSpawn = 0
        val p = player
        if (p != null) {
            p.x = width / 2f
            p.y = height * 0.75f
        }
        startLoopIfNeeded()
        invalidate()
    }

    companion object {
        private const val PLAYER_SPEED = 8f
        private const val ENEMY_RADIUS = 40f
        private const val SPAWN_INTERVAL_FRAMES = 40
        private const val ITEM_SPAWN_INTERVAL_FRAMES = 120
        private const val MAX_ITEMS_ON_SCREEN = 5
    }
}
