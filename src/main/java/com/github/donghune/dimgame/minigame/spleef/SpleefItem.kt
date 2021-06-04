package com.github.donghune.dimgame.minigame.spleef

import com.github.donghune.dimgame.minigame.DimGameItem
import com.github.donghune.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SpleefItem : DimGameItem<SpleefItem.Code>() {

    init {
        ItemBuilder().setMaterial(Material.DIAMOND_SHOVEL)
            .build()
            .apply { itemMeta!!.addEnchant(Enchantment.DIG_SPEED, 4, false ) }
            .registerAction(Code.SHOVEL, false)

        ItemBuilder()
            .setMaterial(Material.RABBIT_FOOT)
            .setDisplay("점프")
            .setLore(listOf("4칸 높이를 한 번에 점프합니다."))
            .build()
            .registerAction(Code.JUMP, true) {
                val potionEffect = PotionEffect(PotionEffectType.JUMP, 20 * 2, 3, true, false)
                it.player.addPotionEffect(potionEffect)
            }

        ItemBuilder()
            .setMaterial(Material.FEATHER)
            .setDisplay("신속")
            .setLore(listOf("3초간 신속1이 부여됩니다."))
            .build()
            .registerAction(Code.SPEED, true) {
                val potionEffect = PotionEffect(PotionEffectType.SPEED, 20 * 3, 0, true, false)
                it.player.addPotionEffect(potionEffect)
            }


        ItemBuilder()
            .setMaterial(Material.POTION)
            .setDisplay("투명")
            .setLore(listOf("2초간 투명이 부여됩니다."))
            .build()
            .registerAction(Code.INVISIBLE, true) {
                val potionEffect = PotionEffect(PotionEffectType.INVISIBILITY, 20 * 2, 1, true, false)
                it.player.addPotionEffect(potionEffect)
            }
    }

    enum class Code {
        SHOVEL, JUMP, SPEED, INVISIBLE
    }
}