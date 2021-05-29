package com.namu.dimgame.minigame.score_of_push

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import java.util.*

class ScoreOfPushScheduler(dimGame: ScoreOfPush) : DimGameScheduler<ScoreOfPushScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            started {
                Bukkit.getOnlinePlayers().forEach { dimGame.bossBar.addPlayer(it) }
            }
            doing {
                dimGame.bossBar.setTitle(
                    "남은시간 %02d:%02d".format(
                        (dimGame.playTime - it) / 60,
                        (dimGame.playTime - it) % 60
                    )
                )
                dimGame.bossBar.progress = 1.0 - (it / dimGame.playTime).toDouble()
                if (it % 20 == 0) {
                    var stPlayer: UUID = UUID.randomUUID()
                    var maxScore = 0

                    dimGame.uuidByScore.keys.forEach { uuid ->
                        if (maxScore < dimGame.uuidByScore[uuid] ?: 0) {
                            maxScore = dimGame.uuidByScore[uuid] ?: 0
                            stPlayer = uuid
                        }
                    }

                    Bukkit.getOnlinePlayers().forEach { player ->
                        player.sendTitle(
                            ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + Bukkit.getPlayer(
                                stPlayer
                            )!!.displayName,
                            ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + "현재 1등 플레이어", 10, 40, 10
                        )
                    }
                }
            }
            finished {
                dimGame.bossBar.players.forEach { dimGame.bossBar.removePlayer(it) }
                dimGame.uuidByScore.toList()
                    .sortedByDescending { it.second }
                    .mapNotNull { Bukkit.getPlayer(it.first) }
                    .toList()
                    .also { dimGame.stopGame(it) }
            }
        }.registerScheduler(Code.MAIN)

        SchedulerManager {
            doing {
                Bukkit.getOnlinePlayers()
                    .filter { dimGame.mapLocations.respawn.distance(it.location) <= 3.5 }
                    .forEach { player ->
                        val uuid = player.uniqueId
                        dimGame.uuidByScore[uuid] = (dimGame.uuidByScore[uuid] ?: 0) + 1
                        player.level = dimGame.uuidByScore[uuid] ?: 0
                        if (it % 4 == 0) {
                            player.playSound(player.location, Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 0.1f, 0.7f)
                        }
                    }
            }
        }.registerScheduler(Code.SCORE)
    }

    enum class Code {
        MAIN, SCORE
    }
}