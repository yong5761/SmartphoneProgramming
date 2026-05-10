package com.wooin.ladybug.framework

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.wooin.ladybug.R

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        setBackgroundResource(R.drawable.bg_game_world)
    }
}
