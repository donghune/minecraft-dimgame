package com.namu.dimgame.manager

import com.namu.dimgame.minigame.*
import com.namu.dimgame.plugin
import com.namu.dimgame.repository.other.DimGameScoreBoard
import com.namu.dimgame.repository.other.ParticleResources
import com.namu.dimgame.repository.participant.AbstractParticipantRepository
import com.namu.dimgame.repository.participant.ParticipantStatusRepository
import com.namu.dimgame.repository.score.AbstractPlayerScoreRepository
import com.namu.dimgame.repository.score.PlayerScoreRepository
import com.namu.dimgame.util.runOnBukkit
import com.namu.namulibrary.schedular.SchedulerManager
import kotlinx.coroutines.*
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import java.lang.Runnable

class DimGameManager : AbstractDimGameManager() {

    override val lobbyLocation: Location = Location(Bukkit.getWorld("world"), 241.0, 86.0, 263.0)

    override val scoreRepository: AbstractPlayerScoreRepository = PlayerScoreRepository()
    override val participantRepository: AbstractParticipantRepository = ParticipantStatusRepository()

    override var gameState: GameStatus = GameStatus.NOT_PLAYING
    override var roundGameState: RoundGameStatus = RoundGameStatus.WAITING
    override var round: Int = 0

    private val lobbyBossBar =
        Bukkit.createBossBar(NamespacedKey(plugin, "DimGame"), "DimGame", BarColor.YELLOW, BarStyle.SOLID)

    private val lobbyBossBarScheduler: SchedulerManager = SchedulerManager {
        var index = 0
        doing {
            lobbyBossBar.setTitle(ChatColor.BOLD.toString() + colors[index].toString() + "[ DimGame ]")
            index++
            if (colors.size == index) {
                index = 0
            }
            Bukkit.getOnlinePlayers().forEach { lobbyBossBar.addPlayer(it) }
        }
    }

    private val colors = arrayOf(
        ChatColor.WHITE,
        ChatColor.DARK_RED,
        ChatColor.RED,
        ChatColor.GOLD,
        ChatColor.YELLOW,
        ChatColor.GREEN,
        ChatColor.DARK_GREEN,
        ChatColor.AQUA,
        ChatColor.DARK_AQUA,
        ChatColor.BLUE,
        ChatColor.DARK_BLUE,
        ChatColor.LIGHT_PURPLE,
        ChatColor.DARK_PURPLE,
        ChatColor.GRAY,
        ChatColor.DARK_GRAY,
    )

    override fun onEnabled() {
        lobbyBossBarScheduler.runTick(10, Int.MAX_VALUE)
    }

    override fun start() {
        lobbyBossBarScheduler.stopScheduler()
        Bukkit.getOnlinePlayers().forEach {
            lobbyBossBar.removePlayer(it)
        }
        Bukkit.setWhitelist(true)
        round = 0
        gameState = GameStatus.PLAYING
        dimGameList = getNewGameList().shuffled()
        roundGameState = RoundGameStatus.WAITING
        scoreRepository.clearAllPlayerScore()
        Bukkit.getOnlinePlayers().forEach { player -> player.teleport(lobbyLocation) }
        startRound(10)
    }

    override fun stop() {
        Bukkit.getOnlinePlayers().forEach {
            it.teleport(lobbyLocation)
            it.gameMode = GameMode.ADVENTURE
        }
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            lobbyBossBarScheduler.runTick(10, Int.MAX_VALUE)
            gameState = GameStatus.NOT_PLAYING
            Bukkit.setWhitelist(false)
            scoreRepository.showMVPPlayer()
            scoreRepository.clearAllPlayerScore()
            ParticleResources.executeMVPParticle(lobbyLocation)
            DimGameScoreBoard.clearPlayerScoreBoard()
        }, 20L)
    }

    override fun skip() {
        currentDimGame.skipGame()
        startRound(5)
    }

    override fun startRound(countdownTime: Int) {
        if (round == dimGameList.size) {
            stop()
            return
        }

        currentDimGame = dimGameList[round]

        selectGame(
            countdownTime,
            onFinished = {
                roundGameState = RoundGameStatus.RUNNING

                currentDimGame.startGame(
                    participantRepository.getParticipantList(),
                    participantRepository.getObserverList()
                ) { rankPlayerList ->
                    roundGameState = RoundGameStatus.WAITING

                    rankPlayerList.forEachIndexed { index, player ->
                        if (index >= 3) {
                            return@forEachIndexed
                        }
                        scoreRepository.modifyPlayerScore(player.uniqueId, 3 - index)
                    }

                    Bukkit.getOnlinePlayers().forEach {
                        it.level = 0
                        it.exp = 0f
                        DimGameScoreBoard.updatePlayerScoreBoard(it, scoreRepository)
                    }
                    round++
                    startRound(5)
                }
            }
        )

    }

    private fun selectGame(countdownTime: Int, onFinished: () -> Unit) {
        var schedulerManager: SchedulerManager? = null
        SchedulerManager {
            doing {
                println("it = [$it]")
                if (it <= (if (countdownTime == 10) 6 else 1)) {
                    schedulerManager?.stopScheduler()
                    schedulerManager = sendRandomGameNameTitleMessage(countdownTime - it)
                } else {
                    schedulerManager?.stopScheduler()
                    sendCurrentGameNameTitleMessage(countdownTime - it)
                }
            }
            finished {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    onFinished.invoke()
                }, 20L)
            }
        }.runSecond(1, countdownTime)
    }

    private fun sendCurrentGameNameTitleMessage(leftTime: Int) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendTitle(currentDimGame.name, leftTime.toString(), 0, 20, 0)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
    }

    private fun sendRandomGameNameTitleMessage(leftTime: Int): SchedulerManager {
        return SchedulerManager {
            doing {
                val randomTitle = dimGameList.map { dimGame -> dimGame.name }.random()
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.sendTitle(randomTitle, leftTime.toString(), 0, 20, 0)
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.3f)
                }
            }
        }.also { it.runTick(2, 10) }
    }

}