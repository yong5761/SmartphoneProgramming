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
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.wooin.ladybug.BgmPlayer
import com.wooin.ladybug.R
import com.wooin.ladybug.SettingsActivity
import com.wooin.ladybug.game.Enemy
import com.wooin.ladybug.game.Item
import com.wooin.ladybug.game.ItemType
import com.wooin.ladybug.game.Jangpan
import com.wooin.ladybug.game.Player
import kotlin.math.sqrt
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    private val popSoundId = soundPool.load(context, R.raw.pop, 1)
    private var popReady = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) popReady = true
        }
    }

    private val density = resources.displayMetrics.density
    private val backgroundBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.bg_grass)
    private var scaledBackground: Bitmap? = null
    private val viewRect = Rect()
    private var player: Player? = null
    private val enemies = mutableListOf<Enemy>()
    private val items = mutableListOf<Item>()
    private val jangpans = mutableListOf<Jangpan>()
    private var framesSinceSpawn = 0
    private var framesSinceItemSpawn = 0
    private var slowFramesLeft = 0
    private var scoreBoostFramesLeft = 0
    private var gameStartNanos = 0L
    private var lastSecs = -1
    private var timeText = "00:00"
    private var score = 0

    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var isTouching: Boolean = false
    private var loopRunning: Boolean = false
    private var isGameOver: Boolean = false

    private val restartButtonRect = RectF()

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f * density
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f * density
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

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
            if (gameStartNanos == 0L) gameStartNanos = frameTimeNanos
            val secs = ((frameTimeNanos - gameStartNanos) / 1_000_000_000L).toInt()
            if (secs != lastSecs) {
                lastSecs = secs
                timeText = "%02d:%02d".format(secs / 60, secs % 60)
                score += SCORE_PER_SEC * if (scoreBoostFramesLeft > 0) 2 else 1
            }
            if (isTouching) movePlayerTowardTarget(p)
            updateEnemies()
            if (p.barrierType == ItemType.SHIELD) applyShieldEffect(p)
            if (p.hasLifeBarrier) applyLifeEffect(p)
            updateJangpans()
            p.tickBarrier()
            updateSlow()
            updateScoreBoost()
            updateItems()
            checkItemPickup(p)
            spawnEnemyIfDue()
            spawnItemIfDue()
            if (checkCollision(p)) {
                isGameOver = true
                BgmPlayer.stop()
            }
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
        scaledBackground?.recycle()
        scaledBackground = Bitmap.createScaledBitmap(backgroundBitmap, w, h, true)
        layoutRestartButton(w, h)
        fitGameOverTextSize(w)
        if (player == null) {
            player = Player(x = w / 2f, y = h * 0.75f)
            startLoopIfNeeded()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaledBackground?.let { canvas.drawBitmap(it, 0f, 0f, null) }
            ?: canvas.drawBitmap(backgroundBitmap, null, viewRect, null)
        jangpans.forEach { it.draw(canvas) }
        enemies.forEach { it.draw(canvas) }
        items.forEach { it.draw(canvas) }
        player?.draw(canvas, targetX, targetY)
        val p = 16f * density
        canvas.drawText(timeText, p, p + timePaint.textSize, timePaint)
        canvas.drawText("$score", width - p, p + scorePaint.textSize, scorePaint)
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
        scaledBackground?.recycle()
        scaledBackground = null
        soundPool.release()
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
        val speedMult = if (slowFramesLeft > 0) SLOW_SPEED_FACTOR else 1f
        val it = enemies.iterator()
        while (it.hasNext()) {
            val e = it.next()
            e.update(speedMult)
            if (e.y - e.radius > height) it.remove()
        }
    }

    private fun updateItems() {
        items.forEach { it.update(width, height) }
    }

    private fun spawnEnemyIfDue() {
        framesSinceSpawn++
        val base = (SPAWN_INTERVAL_FRAMES - lastSecs / 15 * 3).coerceAtLeast(MIN_SPAWN_INTERVAL_FRAMES)
        val interval = if (slowFramesLeft > 0) SLOW_SPAWN_INTERVAL else base
        if (framesSinceSpawn < interval) return
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
        val type = ITEM_TYPES[Random.nextInt(ITEM_TYPES.size)]
        items.add(Item(x = x, y = y, radius = r, vx = vx, vy = vy, type = type))
    }

    private fun checkItemPickup(p: Player) {
        val it = items.iterator()
        while (it.hasNext()) {
            val item = it.next()
            val dx = p.x - item.x
            val dy = p.y - item.y
            val rsum = p.radius + item.radius
            if (dx * dx + dy * dy <= rsum * rsum) {
                when (item.type) {
                    ItemType.JANGPAN -> jangpans.add(Jangpan(item.x, item.y, JANGPAN_RADIUS, JANGPAN_FRAMES))
                    ItemType.SLOW -> slowFramesLeft = SLOW_FRAMES
                    ItemType.LIFE -> if (!p.hasLifeBarrier) p.hasLifeBarrier = true
                    ItemType.SCORE -> scoreBoostFramesLeft = SCORE_BOOST_FRAMES
                    else -> {
                        p.barrierType = item.type
                        p.barrierFramesLeft = BARRIER_SHIELD_FRAMES
                    }
                }
                it.remove()
            }
        }
    }

    private fun updateJangpans() {
        val jit = jangpans.iterator()
        while (jit.hasNext()) {
            val jangpan = jit.next()
            jangpan.framesLeft--
            if (jangpan.framesLeft <= 0) {
                jit.remove()
                continue
            }
            val eit = enemies.iterator()
            while (eit.hasNext()) {
                val e = eit.next()
                val dx = e.x - jangpan.x
                val dy = e.y - jangpan.y
                if (dx * dx + dy * dy <= jangpan.radius * jangpan.radius) {
                    eit.remove()
                    score += SCORE_PER_KILL * if (scoreBoostFramesLeft > 0) 2 else 1
                    playPopSound()
                }
            }
        }
    }

    private fun playPopSound() {
        if (!popReady) return
        val prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsActivity.KEY_SFX_ENABLED, true)) return
        val vol = prefs.getInt(SettingsActivity.KEY_SFX_VOLUME, 50) / 100f
        soundPool.play(popSoundId, vol, vol, 0, 0, 1f)
    }

    private fun updateSlow() {
        if (slowFramesLeft > 0) slowFramesLeft--
    }

    private fun updateScoreBoost() {
        if (scoreBoostFramesLeft > 0) scoreBoostFramesLeft--
    }

    private fun applyLifeEffect(p: Player) {
        val it = enemies.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val dx = p.x - e.x
            val dy = p.y - e.y
            val rsum = p.lifeBarrierRadius + e.radius
            if (dx * dx + dy * dy <= rsum * rsum) {
                it.remove()
                p.hasLifeBarrier = false
                score += SCORE_PER_KILL * if (scoreBoostFramesLeft > 0) 2 else 1
                playPopSound()
                return
            }
        }
    }

    private fun applyShieldEffect(p: Player) {
        val it = enemies.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val dx = p.x - e.x
            val dy = p.y - e.y
            val rsum = p.barrierRadius + e.radius
            if (dx * dx + dy * dy <= rsum * rsum) {
                it.remove()
                score += SCORE_PER_KILL * if (scoreBoostFramesLeft > 0) 2 else 1
                playPopSound()
            }
        }
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
        canvas.drawText("점수: $score", width / 2f, height / 2f + 10f * density, restartButtonTextPaint)
        val cornerRadius = 20f * density
        canvas.drawRoundRect(restartButtonRect, cornerRadius, cornerRadius, restartButtonFillPaint)
        val textY = restartButtonRect.centerY() -
            (restartButtonTextPaint.descent() + restartButtonTextPaint.ascent()) / 2
        canvas.drawText("다시?", restartButtonRect.centerX(), textY, restartButtonTextPaint)
    }

    private fun restartGame() {
        isGameOver = false
        isTouching = false
        BgmPlayer.restartFromBeginning()
        enemies.clear()
        items.clear()
        jangpans.clear()
        framesSinceSpawn = 0
        framesSinceItemSpawn = 0
        slowFramesLeft = 0
        scoreBoostFramesLeft = 0
        gameStartNanos = 0L
        lastSecs = -1
        timeText = "00:00"
        score = 0
        val p = player
        if (p != null) {
            p.x = width / 2f
            p.y = height * 0.75f
            p.barrierType = null
            p.barrierFramesLeft = 0
            p.hasLifeBarrier = false
        }
        startLoopIfNeeded()
        invalidate()
    }

    companion object {
        private val ITEM_TYPES = ItemType.entries.toTypedArray()
        private const val PLAYER_SPEED = 8f
        private const val ENEMY_RADIUS = 40f
        private const val SPAWN_INTERVAL_FRAMES = 40
        private const val ITEM_SPAWN_INTERVAL_FRAMES = 120
        private const val MAX_ITEMS_ON_SCREEN = 5
        private const val BARRIER_SHIELD_FRAMES = 300
        private const val JANGPAN_RADIUS = 250f
        private const val JANGPAN_FRAMES = 180
        private const val MIN_SPAWN_INTERVAL_FRAMES = 15
        private const val SCORE_PER_SEC = 1
        private const val SCORE_PER_KILL = 10
        private const val SCORE_BOOST_FRAMES = 300
        private const val SLOW_FRAMES = 300
        private const val SLOW_SPEED_FACTOR = 0.3f
        private val SLOW_SPAWN_INTERVAL = (SPAWN_INTERVAL_FRAMES / SLOW_SPEED_FACTOR).toInt()
    }
}
