package com.namu.dimgame.minigame

import com.namu.dimgame.manager.PlayerStatus
import org.bukkit.Bukkit
import java.util.*

class PlayerGameStatusManager(val dimGame: DimGame<*, *>) {

    private val uuidByPlayerStatus: MutableMap<UUID, PlayerStatus> = mutableMapOf()

    fun removeStatus(uuid: UUID) {
        uuidByPlayerStatus.remove(uuid)
    }

    fun setStatus(uuid: UUID, playerState: PlayerStatus) {
        uuidByPlayerStatus[uuid] = playerState
        dimGame.onChangedPlayerState(Bukkit.getPlayer(uuid)!!, playerState)
    }

    fun getStatus(uuid: UUID): PlayerStatus {
        if (uuidByPlayerStatus[uuid] == null) {
            setStatus(uuid, PlayerStatus.ALIVE)
        }
        return uuidByPlayerStatus[uuid]!!
    }

}