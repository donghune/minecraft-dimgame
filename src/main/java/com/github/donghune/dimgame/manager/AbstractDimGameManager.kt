package com.github.donghune.dimgame.manager

import com.github.donghune.dimgame.minigame.MiniGame
import com.github.donghune.dimgame.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent

abstract class AbstractDimGameManager : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (roundGameState == RoundGameStatus.WAITING) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (gameState == GameStatus.NOT_PLAYING) {
            event.player.teleport(lobbyLocation)
        }
    }

    abstract val lobbyLocation: Location

    abstract var gameState: GameStatus
    abstract var roundGameState: RoundGameStatus
    abstract var round: Int
    lateinit var dimGameList: List<MiniGame<*, *>>
    lateinit var currentDimGame: MiniGame<*, *>

    abstract fun onEnabled()

    abstract fun start()

    abstract fun stop()

    abstract fun skip()

    abstract fun startRound(countdownTime: Int)

}