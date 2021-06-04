package com.github.donghune.dimgame.minigame.score_of_push

import com.github.donghune.dimgame.minigame.DimGameItem
import com.github.donghune.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class ScoreOfPushItem : DimGameItem<ScoreOfPushItem.Code>() {

    init {
        ItemBuilder().setMaterial(Material.STICK)
            .setDisplay("막대기")
            .setLore(listOf("이걸로 상대방을 밀어버리자"))
            .build()
            .apply {
                addUnsafeEnchantment(Enchantment.KNOCKBACK, 3)
            }
            .registerAction(Code.STICK, false)
    }

    enum class Code {
        STICK
    }
}