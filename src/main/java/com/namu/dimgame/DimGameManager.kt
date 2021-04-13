package com.namu.dimgame

import com.namu.dimgame.entity.DimGame
import com.namu.dimgame.entity.GameState
import com.namu.dimgame.entity.PlayerState
import com.namu.dimgame.game.*
import com.namu.dimgame.schedular.MiniGameScheduler
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object DimGameManager : Listener {

    // config
    private val LOBBY_LOCATION: Location = Location(Bukkit.getWorld("world"), 232.0, 86.0, 262.0)
    private const val MAX_ROUND = 1
    private val LOADED_GAME_LIST = listOf(
        Spleef(),
    ).shuffled()

    private var currentRound: Int = 0
    private var gameState: GameState = GameState.NOT_PLAYING
    private val isObserverByUUID: MutableMap<UUID, Boolean> = mutableMapOf()
    private var selectedGameList: MutableList<DimGame> = mutableListOf()

    private lateinit var dimGame: DimGame

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun startGame(): Boolean {
        Bukkit.getServer().setWhitelist(true)

        currentRound = 0

        if (gameState == GameState.PLAYING || gameState == GameState.NEXT_WAITING) {
            return false
        }

        selectedGameList.clear()
        selectedGameList = LOADED_GAME_LIST.shuffled().subList(0, MAX_ROUND) as MutableList<DimGame>

        executeMiniGameProcess()
        return true
    }

    fun stopGame(isForce: Boolean): Boolean {
        Bukkit.getServer().setWhitelist(false)

        if (gameState == GameState.NOT_PLAYING) {
            return false
        }

        gameState = GameState.NOT_PLAYING

        if (isForce) {
            dimGame.stopMiniGame(listOf())
        }

        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.ADVENTURE
            it.teleport(LOBBY_LOCATION)
            it.inventory.clear()
        }

        return true
    }

    private fun executeMiniGameProcess() {
        gameState = GameState.NEXT_WAITING

        val participationPlayerList = Bukkit.getOnlinePlayers().filter(::isParticipation).toList()
        val observerPlayerList = Bukkit.getOnlinePlayers().filter(::isObserver).toList()
        dimGame = selectedGameList[currentRound]

        gameState = GameState.PLAYING

        MiniGameScheduler(
            gameName = selectedGameList[currentRound].name,
            onStart = {

            },
            onFinish = {
                dimGame.startMiniGame(
                    participationPlayerList = participationPlayerList,
                    observerPlayerList = observerPlayerList,
                    onMiniGameStopCallback = {
                        if (currentRound == MAX_ROUND) {
                            stopGame(false)
                            return@startMiniGame
                        }

                        gameState = GameState.NEXT_WAITING
                        Bukkit.getOnlinePlayers().forEach { it.teleport(LOBBY_LOCATION) }
                        executeMiniGameProcess()
                    }
                )
                currentRound++
            }
        ).runSecond(1, 5)
    }

    fun addObserver(player: Player) {
        isObserverByUUID[player.uniqueId] = true
    }

    fun removeObserver(player: Player) {
        isObserverByUUID[player.uniqueId] = false
    }

    fun isObserver(player: Player): Boolean {
        return isObserverByUUID[player.uniqueId] ?: false
    }

    private fun isParticipation(player: Player): Boolean {
        return isObserverByUUID[player.uniqueId] == null
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (gameState != GameState.PLAYING) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (gameState == GameState.NOT_PLAYING) {
            event.player.teleport(LOBBY_LOCATION)
        }
    }

}