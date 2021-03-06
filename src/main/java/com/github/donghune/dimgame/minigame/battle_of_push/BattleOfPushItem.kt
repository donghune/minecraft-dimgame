package com.github.donghune.dimgame.minigame.battle_of_push

import com.github.donghune.dimgame.minigame.MiniGameItem
import com.github.donghune.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BattleOfPushItem : MiniGameItem<BattleOfPushItem.Code>() {

    init {
        ItemBuilder().setMaterial(Material.POTION)
            .setDisplay("투명인간")
            .setLore(listOf("공중부양을 5초간 부여합니다."))
            .build()
            .registerAction(Code.INVISIBLE, true) {
                val potionEffect = PotionEffect(PotionEffectType.INVISIBILITY, 20 * 3, 1, true, false)
                it.player.addPotionEffect(potionEffect)
            }

        ItemBuilder().setMaterial(Material.RABBIT_HIDE)
            .setDisplay("신속")
            .setLore(listOf("3초간 신속1이 부여됩니다."))
            .build()
            .registerAction(Code.SPEED, true) {
                val potionEffect = PotionEffect(PotionEffectType.SPEED, 20 * 3, 0, true, false)
                it.player.addPotionEffect(potionEffect)
            }

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
        INVISIBLE, SPEED, STICK
    }

}

