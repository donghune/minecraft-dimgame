package com.namu.dimgame.minigame.games.jump_map

import com.namu.dimgame.minigame.DimGameItem
import com.namu.namulibrary.extension.ItemBuilder
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class JumpMapItem : DimGameItem<JumpMapItem.Code>() {

    init {
        ItemBuilder().setMaterial(Material.FEATHER)
                .setDisplay("날개")
                .setLore(listOf("공중부양을 5초간 부여합니다."))
                .build()
                .registerAction(Code.FLY) {
                    val potionEffect = PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 1, true, false)
                    it.player.addPotionEffect(potionEffect)
                }


        ItemBuilder().setMaterial(Material.RABBIT_FOOT)
                .setDisplay("점프")
                .setLore(listOf("점프강화 2를 3초간 부여합니다."))
                .build()
                .registerAction(Code.JUMP) {
                    val potionEffect = PotionEffect(PotionEffectType.JUMP, 20 * 3, 2, true, false)
                    it.player.addPotionEffect(potionEffect)
                }
    }

    enum class Code {
        FLY,
        JUMP
    }
}