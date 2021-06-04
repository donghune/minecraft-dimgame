package com.github.donghune.dimgame

import com.github.donghune.dimgame.manager.DimGameManager
import com.github.monun.kommand.kommand
import com.github.donghune.namulibrary.schedular.SchedulerManager
import org.bukkit.*
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