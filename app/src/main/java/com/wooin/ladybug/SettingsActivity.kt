package com.wooin.ladybug

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "ladybug_settings"
        const val KEY_BGM_ENABLED = "bgm_enabled"
        const val KEY_BGM_VOLUME = "bgm_volume"
        const val KEY_SFX_ENABLED = "sfx_enabled"
        const val KEY_SFX_VOLUME = "sfx_volume"

        private const val DEFAULT_ENABLED = true
        private const val DEFAULT_VOLUME = 50

        private const val DISABLED_ALPHA = 0.4f
        private const val ENABLED_ALPHA = 1.0f
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val switchBgm = findViewById<SwitchCompat>(R.id.switchBgm)
        val seekBgm = findViewById<SeekBar>(R.id.seekBgm)
        val switchSfx = findViewById<SwitchCompat>(R.id.switchSfx)
        val seekSfx = findViewById<SeekBar>(R.id.seekSfx)

        switchBgm.isChecked = prefs.getBoolean(KEY_BGM_ENABLED, DEFAULT_ENABLED)
        seekBgm.progress = prefs.getInt(KEY_BGM_VOLUME, DEFAULT_VOLUME)
        switchSfx.isChecked = prefs.getBoolean(KEY_SFX_ENABLED, DEFAULT_ENABLED)
        seekSfx.progress = prefs.getInt(KEY_SFX_VOLUME, DEFAULT_VOLUME)

        applySeekBarEnabled(seekBgm, switchBgm.isChecked)
        applySeekBarEnabled(seekSfx, switchSfx.isChecked)

        btnBack.setOnClickListener { finish() }

        switchBgm.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_BGM_ENABLED, isChecked).apply()
            applySeekBarEnabled(seekBgm, isChecked)
            BgmPlayer.applySettings(this)
        }
        switchSfx.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_SFX_ENABLED, isChecked).apply()
            applySeekBarEnabled(seekSfx, isChecked)
        }

        seekBgm.setOnSeekBarChangeListener(volumeSaver(KEY_BGM_VOLUME))
        seekSfx.setOnSeekBarChangeListener(volumeSaver(KEY_SFX_VOLUME))
    }

    private fun applySeekBarEnabled(seekBar: SeekBar, enabled: Boolean) {
        seekBar.isEnabled = enabled
        seekBar.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    private fun volumeSaver(key: String) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                prefs.edit().putInt(key, progress).apply()
                if (key == KEY_BGM_VOLUME) BgmPlayer.applySettings(this@SettingsActivity)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
