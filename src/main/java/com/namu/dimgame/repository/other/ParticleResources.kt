package com.namu.dimgame.repository.other

import com.namu.dimgame.plugin
import org.bukkit.*
import org.bukkit.entity.Firework
import kotlin.random.Random

object ParticleResources {

    fun executeMVPParticle(centerLocation : Location) {
        repeat(20) {
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable {
                    centerLocation.clone().apply {
                        x += Random.nextInt(-10, 10)
                        y += Random.nextInt(-10, 10)
                        z += Random.nextInt(-10, 10)
                    }.also { location ->
                        location.world.spawn(location, Firework::class.java).apply {
                            fireworkMeta = fireworkMeta.apply {
                                addEffect(
                                    FireworkEffect.builder().with(FireworkEffect.Type.values().first())
                                        .withColor(
                                            Color.fromBGR(
                                                Random.nextInt(0, 255),
                                                Random.nextInt(0, 255),
                                                Random.nextInt(0, 255)
                                            )
                                        ).build()
                                )
                                power = 0
                            }
                        }
                        location.world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)
                    }
                },
                Random.nextLong(10, 40)
            )
        }
    }

}