package com.wooin.ladybug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wooin.ladybug.framework.GameView

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(GameView(this))
    }
}
