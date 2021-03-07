package com.namu.dimgame.entity

import com.namu.namulibrary.extension.sendDebugMessage
import org.bukkit.Location
import org.bukkit.entity.Player

data class GameMap(
    val pos1: Location,
    val pos2: Location,
    val respawn: Location
) {
    private val startX = pos1.blockX.coerceAtMost(pos2.blockX)
    private val endX = pos1.blockX.coerceAtLeast(pos2.blockX)

    private val startY = pos1.blockY.coerceAtMost(pos2.blockY)
    private val endY = pos1.blockY.coerceAtLeast(pos2.blockY)

    private val startZ = pos1.blockZ.coerceAtMost(pos2.blockZ)
    private val endZ = pos1.blockZ.coerceAtLeast(pos2.blockZ)

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