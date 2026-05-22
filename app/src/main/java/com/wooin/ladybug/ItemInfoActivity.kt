package com.wooin.ladybug

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ItemInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_info)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.itemContainer)
        val inflater = LayoutInflater.from(this)

        DEFAULT_ITEM_INFOS.forEach { item ->
            val card = inflater.inflate(R.layout.item_info_card, container, false)
            card.findViewById<ImageView>(R.id.ivIcon).setImageResource(item.iconResId)
            card.findViewById<TextView>(R.id.tvItemName).text = item.name
            card.findViewById<TextView>(R.id.tvItemDescription).text = item.description
            container.addView(card)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
