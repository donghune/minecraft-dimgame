package com.namu.dimgame

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.TNT
import org.bukkit.util.Vector


enum class AnvilTNTPattern(private val vectors: List<Vector>) {

    TNT_DOT(
        listOf(
            Vector(3.0, 0.0, -3.0),
            Vector(3.0, 0.0, 0.0),
            Vector(3.0, 0.0, 3.0),
            Vector(0.0, 0.0, -3.0),
            Vector(0.0, 0.0, 0.0),
            Vector(0.0, 0.0, 3.0),
            Vector(-3.0, 0.0, -3.0),
            Vector(-3.0, 0.0, 0.0),
            Vector(-3.0, 0.0, 3.0),
        )
    ),
    TNT_LINE_HORIZONTAL(
        listOf(
            Vector(12.0, 0.0, 0.0),
            Vector(9.0, 0.0, 0.0),
            Vector(6.0, 0.0, 0.0),
            Vector(3.0, 0.0, 0.0),
            Vector(0.0, 0.0, 0.0),
            Vector(-3.0, 0.0, 0.0),
            Vector(-6.0, 0.0, 0.0),
            Vector(-9.0, 0.0, 0.0),
            Vector(-12.0, 0.0, 0.0),
        )
    ),
    TNT_LINE_VERTICAL(
        listOf(
            Vector(0.0, 0.0, 12.0),
            Vector(0.0, 0.0, 9.0),
            Vector(0.0, 0.0, 6.0),
            Vector(0.0, 0.0, 3.0),
            Vector(0.0, 0.0, 0.0),
            Vector(0.0, 0.0, -3.0),
            Vector(0.0, 0.0, -6.0),
            Vector(0.0, 0.0, -9.0),
            Vector(0.0, 0.0, -12.0),
        )
    ),
    TNT_CROSS(
        listOf(
            Vector(2.0, 0.0, 2.0),
            Vector(4.0, 0.0, 4.0),
            Vector(6.0, 0.0, 6.0),
            Vector(8.0, 0.0, 8.0),
            Vector(10.0, 0.0, 10.0),
            Vector(12.0, 0.0, 12.0),
            Vector(-2.0, 0.0, 2.0),
            Vector(-4.0, 0.0, 4.0),
            Vector(-6.0, 0.0, 6.0),
            Vector(-8.0, 0.0, 8.0),
            Vector(-10.0, 0.0, 10.0),
            Vector(-12.0, 0.0, 12.0),
            Vector(2.0, 0.0, -2.0),
            Vector(4.0, 0.0, -4.0),
            Vector(6.0, 0.0, -6.0),
            Vector(8.0, 0.0, -8.0),
            Vector(10.0, 0.0, -10.0),
            Vector(12.0, 0.0, -12.0),
            Vector(-2.0, 0.0, -2.0),
            Vector(-4.0, 0.0, -4.0),
            Vector(-6.0, 0.0, -6.0),
            Vector(-8.0, 0.0, -8.0),
            Vector(-10.0, 0.0, -10.0),
            Vector(-12.0, 0.0, -12.0),
            Vector(0.0, 0.0, 0.0),
        )
    );

    fun getWidth(): Int {
        return (vectors.maxOf { it.x } - vectors.minOf { it.x }).toInt()
    }

    fun getHeight(): Int {
        return (vectors.maxOf { it.z } - vectors.minOf { it.z }).toInt()
    }

    fun execute(centerLocation: Location) {
        vectors.forEach {
            val blockLocation = centerLocation.clone().add(it)
            blockLocation.world!!.spawnFallingBlock(
                blockLocation,
                (Material.TNT.createBlockData() as TNT).also { tnt -> tnt.isUnstable = true }
            )
        }
    }

}