package com.github.donghune.dimgame.repository.ingame

import com.github.donghune.dimgame.events.PlayerMiniGameAliveEvent
import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.events.PlayerStatusChangeEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import org.bukkit.Bukkit
import java.util.*

object PlayerMiniGameStatusRepository {

    private val uuidByMiniGameStatus: MutableMap<UUID, PlayerMiniGameStatus> = mutableMapOf()

    fun removeStatus(uuid: UUID) {
        uuidByMiniGameStatus.remove(uuid)
    }

    fun setStatus(uuid: UUID, playerMiniGameState: PlayerMiniGameStatus) {
        val isEqualBefore = uuidByMiniGameStatus[uuid] == playerMiniGameState
        uuidByMiniGameStatus[uuid] = playerMiniGameState
        Bukkit.getPluginManager().callEvent(PlayerStatusChangeEvent(Bukkit.getPlayer(uuid)!!))

        if (isEqualBefore) {
            return
        }

        if (playerMiniGameState == PlayerMiniGameStatus.ALIVE) {
            Bukkit.getPluginManager().callEvent(PlayerMiniGameAliveEvent(Bukkit.getPlayer(uuid)!!))
        } else {
            Bukkit.getPluginManager().callEvent(PlayerMiniGameDieEvent(Bukkit.getPlayer(uuid)!!))
        }
    }

    fun getStatus(uuid: UUID): PlayerMiniGameStatus {
        if (uuidByMiniGameStatus[uuid] == null) {
            setStatus(uuid, PlayerMiniGameStatus.ALIVE)
        }
        return uuidByMiniGameStatus[uuid]!!
    }

}