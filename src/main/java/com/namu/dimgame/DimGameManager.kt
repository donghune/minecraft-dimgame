package com.namu.dimgame

import com.namu.dimgame.entity.DimGame
import com.namu.dimgame.entity.GameState
import com.namu.dimgame.game.Spleef
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

object DimGameManager : Listener {

    // config
    private val LOBBY_LOCATION: Location = Location(Bukkit.getWorld("world"), 232.0, 86.0, 262.0)
    private const val MAX_ROUND = 1
    private val LOADED_GAME_LIST = listOf<DimGame>(
            Spleef(),
            Spleef(),
            Spleef()
    )

    private var currentRound: Int = 0
    private var gameState: GameState = GameState.NOT_PLAYING
    private val isObserverByUUID: MutableMap<UUID, Boolean> = mutableMapOf()
    private var selectedGameList: MutableList<DimGame> = mutableListOf()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun startGame(): Boolean {

        if (gameState == GameState.PLAYING || gameState == GameState.NEXT_WAITING) {
            return false
        }

        selectedGameList.clear()
        selectedGameList = LOADED_GAME_LIST.shuffled().subList(0, 3) as MutableList<DimGame>

        executeMiniGameProcess()
        return true
    }

    fun stopGame(): Boolean {

        if (gameState == GameState.NOT_PLAYING) {
            return false
        }

        gameState = GameState.NOT_PLAYING

        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.ADVENTURE
            it.teleport(LOBBY_LOCATION)
            it.inventory.clear()
        }

        return true
    }

    private fun executeMiniGameProcess() {
        gameState = GameState.NEXT_WAITING

        if (currentRound == MAX_ROUND) {
            stopGame()
            return
        }

        val participationPlayerList = Bukkit.getOnlinePlayers().filter(::isParticipation).toList()
        val observerPlayerList = Bukkit.getOnlinePlayers().filter(::isObserver).toList()
        val dimGame = selectedGameList[currentRound]

        gameState = GameState.PLAYING
        dimGame.startMiniGame(
                participationPlayerList = participationPlayerList,
                observerPlayerList = observerPlayerList,
                onMiniGameStopCallback = {
                    Bukkit.getOnlinePlayers().forEach { it.teleport(LOBBY_LOCATION) }
                    executeMiniGameProcess()
                }
        )
        currentRound++
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

}