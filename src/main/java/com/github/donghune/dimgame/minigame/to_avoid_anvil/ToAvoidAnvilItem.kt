package com.github.donghune.dimgame.minigame.to_avoid_anvil

import com.github.donghune.dimgame.minigame.MiniGameItem
import com.github.donghune.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ToAvoidAnvilItem : MiniGameItem<ToAvoidAnvilItem.Code>() {
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