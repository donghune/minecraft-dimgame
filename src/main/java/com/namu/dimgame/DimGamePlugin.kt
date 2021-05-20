package com.namu.dimgame

import com.github.noonmaru.kommand.kommand
import com.namu.dimgame.manager.DimGameManager
import com.namu.namulibrary.extension.sendErrorMessage
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.plugin.java.JavaPlugin

lateinit var plugin: JavaPlugin

class DimGamePlugin : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        SchedulerManager.initializeSchedulerManager(this)
        val dimGameManager = DimGameManager()

        Bukkit.getOnlinePlayers().forEach {
            it.activePotionEffects.forEach { potionEffect ->
                it.removePotionEffect(potionEffect.type)
            }
        }

        kommand {
            register("dimgame") {
                then("start") {
                    executes {
                        val result = dimGameManager.start()

//                        if (!result) {
//                            it.sender.sendErrorMessage("이미 게임이 진행중입니다.")
//                            return@executes
//                        }
                    }
                }
                then("stop") {
                    executes {
                        val result = dimGameManager.stop()

//                        if (!result) {
//                            it.sender.sendErrorMessage("게임이 진행중이지 않습니다.")
//                            return@executes
//                        }
                    }
                }
                then("skip") {
                    executes {
                        dimGameManager.skip()
                    }
                }
//                then("ob") {
//                    then("player" to player()) {
//                        executes {
//                            val target = it.parseArgument<Player>("player")
//                            val playerState = DimGameManager.getPlayerGameState(target.uniqueId)
//
//                            if (playerState == ParticipantStatus.NONE || playerState == ParticipantStatus.OBSERVER) {
//                                DimGameManager.setPlayerGameState(target.uniqueId, ParticipantStatus.PARTICIPANT)
//                                target.sendInfoMessage("참여자로 설정 되었습니다.")
//                                return@executes
//                            }
//
//                            DimGameManager.setPlayerGameState(target.uniqueId, ParticipantStatus.OBSERVER)
//                            target.sendInfoMessage("옵저버로 설정 되었습니다.")
//                        }
//                    }
//                }
            }
        }
    }

    override fun onDisable() {
    }
}