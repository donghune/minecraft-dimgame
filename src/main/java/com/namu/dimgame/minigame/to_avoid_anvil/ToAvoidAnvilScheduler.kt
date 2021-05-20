package com.namu.dimgame.minigame.to_avoid_anvil

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.namulibrary.extension.replaceChatColorCode
import com.namu.namulibrary.schedular.SchedulerManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit

class ToAvoidAnvilScheduler(dimGame: ToAvoidAnvil) : DimGameScheduler<ToAvoidAnvilScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            doing {
                val timeCycle: Int
                val anvilCount: Int
                when (it) {
                    in 0..16 -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.spigot().sendMessage(
                                    ChatMessageType.ACTION_BAR,
                                    TextComponent("하늘에서 모루가 떨어집니다. [$it]")
                            )
                        }
                        timeCycle = 5
                        anvilCount = 30
                    }
                    in 16..25 -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.spigot().sendMessage(
                                    ChatMessageType.ACTION_BAR,
                                    TextComponent("&7하늘에서 모루가 소나기 같이 떨어집니다. [$it]".replaceChatColorCode())
                            )
                        }
                        timeCycle = 3
                        anvilCount = 30
                    }
                    in 25..45 -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.spigot().sendMessage(
                                    ChatMessageType.ACTION_BAR,
                                    TextComponent("&b하늘에서 모루가 폭풍같이 떨어집니다. [$it]".replaceChatColorCode())
                            )
                        }
                        timeCycle = 2
                        anvilCount = 30
                    }
                    else -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.spigot().sendMessage(
                                    ChatMessageType.ACTION_BAR,
                                    TextComponent("&c하늘이 붉게 변하자 모루가 미친듯이 떨어집니다. [$it]".replaceChatColorCode())
                            )
                        }
                        timeCycle = 1
                        anvilCount = 30
                    }
                }
                if (it % timeCycle == 0) {
                    repeat(anvilCount) {
                        dimGame.spawnAnvil()
                    }
                }
            }
        }.registerScheduler(Code.MAIN)
    }

    enum class Code {
        MAIN
    }
}