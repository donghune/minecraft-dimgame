package com.github.donghune.dimgame.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox

fun BoundingBox.getBlocks(world: World): List<Location> {
    val list = mutableListOf<Location>()
    for (x in minX.toInt()..maxX.toInt()) {
        for (z in minZ.toInt()..maxZ.toInt()) {
            for (y in minY.toInt()..maxY.toInt()) {
                list.add(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
            }
        }
    }
    return list.toList()
}

fun BoundingBox.getBottom(): BoundingBox {
    return clone().resize(minX, minY, minZ, maxX, minY, maxZ)
}