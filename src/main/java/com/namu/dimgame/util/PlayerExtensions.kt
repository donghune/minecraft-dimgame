package com.namu.dimgame.util

import com.namu.dimgame.plugin
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

fun Player.clearChat() {
    (1..20).forEach { _ ->
        sendMessage("")
    }
}

fun SchedulerManager.stopSchedulerNotWithFinish() {
    doingBukkitTask.cancel()
    cancelBukkitTask.cancel()
}

fun runOnBukkit(block: () -> Unit) = plugin.server.scheduler.scheduleSyncDelayedTask(plugin, block)

fun BoundingBox.setMinX(value: Double): BoundingBox {
    return resize(value, minY, minZ, maxX, maxY, maxZ)
}

fun BoundingBox.setMinY(value: Double): BoundingBox {
    return resize(minX, value, minZ, maxX, maxY, maxZ)
}

fun BoundingBox.setMinZ(value: Double): BoundingBox {
    return resize(minX, minY, value, maxX, maxY, maxZ)
}

fun BoundingBox.setMaxX(value: Double): BoundingBox {
    return resize(minX, minY, minZ, value, maxY, maxZ)
}

fun BoundingBox.setMaxY(value: Double): BoundingBox {
    return resize(minX, minY, minZ, maxX, value, maxZ)
}

fun BoundingBox.setMaxZ(value: Double): BoundingBox {
    return resize(minX, minY, minZ, maxX, maxY, value)
}

fun BoundingBox.getTop(): BoundingBox {
    return clone().resize(minX, maxY, minZ, maxX, maxY, maxZ)
}

fun BoundingBox.toRandomLocation(world: World = Bukkit.getWorld("world")!!): Location {
    return Location(
        world,
        (minX.toInt()..maxX.toInt()).random().toDouble(),
        (minY.toInt()..maxY.toInt()).random().toDouble(),
        (minZ.toInt()..maxZ.toInt()).random().toDouble()
    )
}

fun BoundingBox.contains2D(vector: Vector): Boolean {
    if (vector.x in minX..maxX) {
        if (vector.z in minZ..maxZ) {
            return true
        }
    }
    return false
}

fun BoundingBox.resize2D(width: Double, height: Double): BoundingBox {
    println("resize2D() called with: width = $width, height = $height")
    return clone().apply {
        resize(
            minX - width,
            minY,
            minZ - height,
            maxX + width,
            maxY,
            maxZ + height
        )
    }
}