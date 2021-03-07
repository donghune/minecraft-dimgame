package com.namu.dimgame

import com.namu.namulibrary.command.argument.player
import com.namu.namulibrary.command.kommand
import com.namu.namulibrary.extension.sendErrorMessage
import com.namu.namulibrary.extension.sendInfoMessage
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

lateinit var plugin : JavaPlugin

class DimGamePlugin : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        SchedulerManager.initializeSchedulerManager(this)

        kommand {
            register("dimgame") {
                then("start") {
                    executes {
                        val result = DimGameManager.startGame()

                        if (!result) {
                            it.sender.sendErrorMessage("이미 게임이 진행중입니다.")
                            return@executes
                        }
                    }
                }
                then("stop") {
                    executes {
                        val result = DimGameManager.stopGame()

                        if (!result) {
                            it.sender.sendErrorMessage("게임이 진행중이지 않습니다.")
                            return@executes
                        }
                    }
                }
                then("ob") {
                    then("player" to player()) {
                        executes {
                            val target = it.parseArgument<Player>("player")
                            if (DimGameManager.isObserver(target)) {
                                DimGameManager.removeObserver(target)
                                target.sendInfoMessage("참여자로 설정 되었습니다.")
                                return@executes
                            }
                            DimGameManager.addObserver(target)
                            target.sendInfoMessage("옵저버로 설정 되었습니다.")
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
    }
}