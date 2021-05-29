package com.namu.dimgame

import com.github.noonmaru.kommand.argument.string
import com.github.noonmaru.kommand.kommand
import com.namu.dimgame.manager.DimGameManager
import com.namu.dimgame.util.getTop
import com.namu.dimgame.util.resize2D
import com.namu.dimgame.util.toRandomLocation
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BoundingBox

lateinit var plugin: JavaPlugin

class DimGamePlugin : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        SchedulerManager.initializeSchedulerManager(this)
        val dimGameManager = DimGameManager()
        dimGameManager.onEnabled()

        Bukkit.getOnlinePlayers().forEach {
            it.activePotionEffects.forEach { potionEffect ->
                it.removePotionEffect(potionEffect.type)
            }
        }

        kommand {
            register("dimgame") {
                then("start") {
                    executes {
                        dimGameManager.start()
                    }
                }
                then("stop") {
                    executes {
                        dimGameManager.stop()
                    }
                }
                then("skip") {
                    executes {
                        dimGameManager.skip()
                    }
                }
                then("debug") {
                    executes {

                    }
                }
            }
        }
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player ->
            Bukkit.getBossBars().forEach { keyedBossBar ->
                keyedBossBar.removePlayer(player)
            }
        }
    }
}