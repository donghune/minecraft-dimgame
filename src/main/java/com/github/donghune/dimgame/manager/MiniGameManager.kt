package com.github.donghune.dimgame.manager

import com.github.donghune.dimgame.events.MiniGameEndEvent
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.repository.other.DimGameScoreBoard
import com.github.donghune.dimgame.repository.other.ParticleResources
import com.github.donghune.dimgame.repository.participant.ParticipantStatusRepository
import com.github.donghune.dimgame.repository.score.PlayerScoreRepository
import com.github.donghune.dimgame.util.syncGameMode
import com.github.donghune.dimgame.util.syncTeleport
import com.github.donghune.namulibrary.extension.addY
import com.github.donghune.namulibrary.schedular.SchedulerManager
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.event.EventHandler
import java.lang.Runnable

class MiniGameManager : AbstractDimGameManager() {

    override val lobbyLocation: Location = Location(Bukkit.getWorld("world"), 241.0, 86.0, 263.0)

    override var gameState: GameStatus = GameStatus.NOT_PLAYING
    override var roundGameState: RoundGameStatus = RoundGameStatus.WAITING
    override var round: Int = 0

    private val lobbyBossBar = Bukkit.createBossBar(
        NamespacedKey(plugin, "DimGame"),
        "DimGame",
        BarColor.YELLOW,
        BarStyle.SOLID
    )

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

    override suspend fun onEnabled() {
        lobbyBossBarScheduler.runTick(10, Int.MAX_VALUE)
        Bukkit.getPluginManager().registerSuspendingEvents(this, plugin)
    }

    override suspend fun start() {
        lobbyBossBarScheduler.stopScheduler()
        Bukkit.setWhitelist(true)
        round = 0
        gameState = GameStatus.PLAYING
        dimGameList = getNewGameList().shuffled()
        roundGameState = RoundGameStatus.WAITING
        PlayerScoreRepository.clearAllPlayerScore()
        Bukkit.getOnlinePlayers().forEach { player ->
            lobbyBossBar.removePlayer(player)
            player.gameMode = GameMode.ADVENTURE
            player.teleport(lobbyLocation)
        }
        startRound()
    }

    override suspend fun stop() {
        Bukkit.getOnlinePlayers().forEach {
            it.syncTeleport(lobbyLocation)
            it.syncGameMode(GameMode.ADVENTURE)
        }
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            lobbyBossBarScheduler.runTick(10, Int.MAX_VALUE)
            gameState = GameStatus.NOT_PLAYING
            Bukkit.setWhitelist(false)
            PlayerScoreRepository.showMVPPlayer()
            PlayerScoreRepository.clearAllPlayerScore()
            ParticleResources.executeMVPParticle(lobbyLocation)
            DimGameScoreBoard.clearPlayerScoreBoard()
        }, 20L)
    }

    override suspend fun skip() {
        currentDimGame.skipGame()
        startRound()
    }

    override suspend fun startRound() {
        if (round == dimGameList.size) {
            stop()
            return
        }

        currentDimGame = dimGameList[round]

        selectGame()

        roundGameState = RoundGameStatus.RUNNING

        currentDimGame.startGame(
            ParticipantStatusRepository.getParticipantList(),
            ParticipantStatusRepository.getObserverList()
        )
    }

    @EventHandler
    suspend fun onMiniGameEndEvent(event: MiniGameEndEvent) {
        roundGameState = RoundGameStatus.WAITING

        event.rank.forEachIndexed { index, uuid ->
            if (index >= 3) {
                return@forEachIndexed
            }
            PlayerScoreRepository.modifyPlayerScore(uuid, 3 - index)
        }

        Bukkit.getOnlinePlayers().forEach {
            it.level = 0
            it.exp = 0f
            DimGameScoreBoard.updatePlayerScoreBoard(it, PlayerScoreRepository)
        }
        round++

        startRound()
    }

    private suspend fun selectGame() {
        // random tick
        repeat(2) { main ->
            repeat(10) {
                val randomTitle = dimGameList.map { dimGame -> dimGame.name }.random()
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.sendTitle(randomTitle, (5 - main).toString(), 0, 20, 0)
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.3f)
                }
                delay(100L)
            }
        }
        // main tick
        repeat(3) {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.sendTitle(currentDimGame.name, (3 - it).toString(), 0, 20, 0)
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            }
            delay(1000L)
        }
    }

}