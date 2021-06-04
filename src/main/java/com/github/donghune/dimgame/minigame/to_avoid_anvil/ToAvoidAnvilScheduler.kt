package com.github.donghune.dimgame.minigame.to_avoid_anvil

import com.github.donghune.dimgame.minigame.DimGameScheduler
import com.github.donghune.dimgame.util.getTop
import com.github.donghune.dimgame.util.resize2D
import com.github.donghune.dimgame.util.toRandomLocation
import com.github.donghune.namulibrary.extension.replaceChatColorCode
import com.github.donghune.namulibrary.schedular.SchedulerManager
import net.kyori.adventure.text.Component
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
                            player.sendActionBar(Component.text("하늘에서 모루가 떨어집니다. [$it]"))
                        }
                        timeCycle = 5
                        anvilCount = 30
                    }
                    in 16..25 -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.sendActionBar(Component.text("&7하늘에서 모루가 소나기 같이 떨어집니다. [$it]".replaceChatColorCode()))
                        }
                        timeCycle = 3
                        anvilCount = 30
                    }
                    in 25..45 -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.sendActionBar(Component.text("&b하늘에서 모루가 폭풍같이 떨어집니다. [$it]".replaceChatColorCode()))
                        }
                        timeCycle = 2
                        anvilCount = 30
                    }
                    else -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.sendActionBar(Component.text("&c하늘이 붉게 변하자 모루가 미친듯이 떨어집니다. [$it]".replaceChatColorCode()))
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

        SchedulerManager {
            doing {
                com.github.donghune.dimgame.AnvilTNTPattern.values().random()
                    .also { println("selected pattern ${toString()}") }
                    .run {
                        dimGame.mapLocations.area
                            .getTop()
                            .also {
                                println("expand before $it")
                            }
                            .resize2D(
                                -(getWidth()).toDouble() / 2,
                                -(getHeight()).toDouble() / 2,
                            )
                            .also {
                                println("expand after $it")
                                execute(it.toRandomLocation())
                            }
                    }
            }
        }.registerScheduler(Code.PATTERN)
    }

    enum class Code {
        MAIN, PATTERN
    }
}