package com.github.donghune.dimgame.minigame

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

data class MiniGameMap(
    val area: BoundingBox,
    val respawn: Location,
) {
    fun isIn(player: Player): Boolean {
        return area.contains(player.location.toVector())
    }

    fun isOut(player: Player): Boolean {
        return !isIn(player)
    }
}