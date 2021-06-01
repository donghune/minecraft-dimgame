package com.namu.dimgame

import com.github.namu0240.namulibrary.schedular.SchedulerManager
import com.github.noonmaru.kommand.kommand
import com.namu.dimgame.manager.DimGameManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

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