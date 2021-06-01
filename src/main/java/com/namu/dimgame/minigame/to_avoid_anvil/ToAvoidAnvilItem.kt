package com.namu.dimgame.minigame.to_avoid_anvil

import com.namu.dimgame.minigame.DimGameItem
import com.github.namu0240.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ToAvoidAnvilItem : DimGameItem<ToAvoidAnvilItem.Code>() {
    init {
        ItemBuilder().setMaterial(Material.COAL)
            .setDisplay("점프")
            .setLore(listOf("점프강화 2를 1초간 줍니다."))
            .build()
            .registerAction(Code.JUMP, true) {
                it.player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.JUMP,
                        20,
                        1,
                        false, false, false
                    )
                )
            }
    }

    enum class Code {
        JUMP
    }
}