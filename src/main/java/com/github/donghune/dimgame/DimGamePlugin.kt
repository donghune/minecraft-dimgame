package com.github.donghune.dimgame

import com.github.donghune.dimgame.manager.MiniGameManager
import com.github.donghune.dimgame.util.activePotionEffectsClear
import com.github.monun.kommand.kommand
import com.github.donghune.namulibrary.schedular.SchedulerManager
import com.github.shynixn.mccoroutine.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.github.shynixn.mccoroutine.scope
import org.bukkit.*
import org.bukkit.plugin.java.JavaPlugin

lateinit var plugin: JavaPlugin

class DimGamePlugin : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        plugin = this

        SchedulerManager.initializeSchedulerManager(this)
        val dimGameManager = MiniGameManager()
        dimGameManager.onEnabled()

        Bukkit.getOnlinePlayers().forEach { it.activePotionEffectsClear() }

        kommand {
            register("dimgame") {
                then("start") {
                    executes {
                        launch(minecraftDispatcher) {
                            dimGameManager.start()
                        }
                    }
                }
                then("stop") {
                    executes {
                        launch(minecraftDispatcher) {
                            dimGameManager.stop()
                        }
                    }
                }
                then("skip") {
                    executes {
                        launch(minecraftDispatcher) {
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
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player ->
            Bukkit.getBossBars().forEach { keyedBossBar ->
                keyedBossBar.removePlayer(player)
            }
        }
    }
}