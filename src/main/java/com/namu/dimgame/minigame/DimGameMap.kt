package com.namu.dimgame.minigame

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

data class DimGameMap(
    val pos1: Location,
    val pos2: Location,
    val respawn: Location
) {
    fun toBoundingBox(): BoundingBox {
        return BoundingBox(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z)
    }

    val startX = pos1.blockX.coerceAtMost(pos2.blockX)
    val endX = pos1.blockX.coerceAtLeast(pos2.blockX)

    val startY = pos1.blockY.coerceAtMost(pos2.blockY)
    val endY = pos1.blockY.coerceAtLeast(pos2.blockY)

    val startZ = pos1.blockZ.coerceAtMost(pos2.blockZ)
    val endZ = pos1.blockZ.coerceAtLeast(pos2.blockZ)

    fun isIn(player: Player): Boolean {
        if (player.location.blockX in startX..endX) {
            if (player.location.blockY in startY..endY) {
                if (player.location.blockZ in startZ..endZ) {
                    return true
                }
            }
        }
        return false
    }

    fun isOut(player: Player): Boolean {
        return !isIn(player)
    }
}