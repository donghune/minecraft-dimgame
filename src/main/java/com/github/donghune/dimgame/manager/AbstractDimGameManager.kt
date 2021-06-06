package com.github.donghune.dimgame.manager

import com.github.donghune.dimgame.minigame.MiniGame
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.util.syncTeleport
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent

abstract class AbstractDimGameManager : Listener {

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (roundGameState == RoundGameStatus.WAITING) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (gameState == GameStatus.NOT_PLAYING) {
            event.player.syncTeleport(lobbyLocation)
        }
    }

    abstract val lobbyLocation: Location

    abstract var gameState: GameStatus
    abstract var roundGameState: RoundGameStatus
    abstract var round: Int
    lateinit var dimGameList: List<MiniGame<*, *>>
    lateinit var currentDimGame: MiniGame<*, *>

    abstract suspend fun onEnabled()

    abstract suspend fun start()

    abstract suspend fun stop()

    abstract suspend fun skip()

    abstract suspend fun startRound()

}