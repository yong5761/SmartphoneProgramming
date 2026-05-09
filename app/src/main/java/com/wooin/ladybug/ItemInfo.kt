package com.wooin.ladybug

import androidx.annotation.DrawableRes

data class ItemInfo(
    val name: String,
    val description: String,
    @DrawableRes val iconResId: Int
)

val DEFAULT_ITEM_INFOS: List<ItemInfo> = listOf(
    ItemInfo(
        name = "장판",
        description = "일정 범위에 장판이 깔려 닿는 적을 제거한다.",
        iconResId = R.drawable.ic_item_jangpan
    ),
    ItemInfo(
        name = "보호막",
        description = "플레이어 주변에 막이 생겨 닿는 적을 제거한다.",
        iconResId = R.drawable.ic_item_shield
    ),
    ItemInfo(
        name = "느림",
        description = "모든 적의 속도가 일정 시간 느려진다.",
        iconResId = R.drawable.ic_item_slow
    ),
    ItemInfo(
        name = "회복",
        description = "추가 생명 1 을 획득한다.",
        iconResId = R.drawable.ic_item_heal
    ),
    ItemInfo(
        name = "점수 부스트",
        description = "일정 시간 점수가 2배로 적용된다.",
        iconResId = R.drawable.ic_item_score
    )
)
