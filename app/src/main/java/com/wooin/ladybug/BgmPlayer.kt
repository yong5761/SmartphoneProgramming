package com.wooin.ladybug

import android.content.Context
import android.media.MediaPlayer

object BgmPlayer {

    private var player: MediaPlayer? = null
    private var stoppedByGameOver = false

    fun ensurePlaying(context: Context) {
        stoppedByGameOver = false
        val prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsActivity.KEY_BGM_ENABLED, true)) return
        val vol = prefs.getInt(SettingsActivity.KEY_BGM_VOLUME, 50) / 100f * BGM_MAX_VOL
        val p = player
        if (p == null) {
            player = MediaPlayer.create(context.applicationContext, R.raw.bgm).apply {
                isLooping = true
                setVolume(vol, vol)
                start()
            }
        } else if (!p.isPlaying) {
            p.setVolume(vol, vol)
            p.seekTo(0)
            p.start()
        }
    }

    fun applySettings(context: Context) {
        val prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(SettingsActivity.KEY_BGM_ENABLED, true)
        val vol = prefs.getInt(SettingsActivity.KEY_BGM_VOLUME, 50) / 100f * BGM_MAX_VOL
        val p = player ?: return
        p.setVolume(vol, vol)
        if (!enabled && p.isPlaying) {
            p.pause()
        } else if (enabled && !p.isPlaying && !stoppedByGameOver) {
            p.start()
        }
    }

    fun stop() {
        stoppedByGameOver = true
        player?.pause()
    }

    fun restartFromBeginning() {
        stoppedByGameOver = false
        player?.apply {
            seekTo(0)
            if (!isPlaying) start()
        }
    }

    private const val BGM_MAX_VOL = 0.4f
}
