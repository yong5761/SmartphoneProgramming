package com.wooin.ladybug

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bmp = BitmapFactory.decodeResource(resources, R.drawable.bg_grass)
        val iv = findViewById<ImageView>(R.id.ivBackground)
        iv.setImageBitmap(bmp)
        iv.colorFilter = PorterDuffColorFilter(0x55000000.toInt(), PorterDuff.Mode.SRC_ATOP)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            overridePendingTransition(0, 0)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(0, 0)
        }

        findViewById<Button>(R.id.btnItemInfo).setOnClickListener {
            startActivity(Intent(this, ItemInfoActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        BgmPlayer.ensurePlaying(this)
        BgmPlayer.applySettings(this)
    }
}
