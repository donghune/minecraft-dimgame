package com.namu.dimgame.minigame

import java.util.*

class PlayerGameStatusManager {

    private val uuidByPlayerStatus: MutableMap<UUID, PlayerStatus> = mutableMapOf()

    fun removeStatus(uuid : UUID) {
        uuidByPlayerStatus.remove(uuid)
    }

    fun setStatus(uuid: UUID, playerState: PlayerStatus) {
        uuidByPlayerStatus[uuid] = playerState
    }

    fun getStatus(uuid: UUID): PlayerStatus {
        if (uuidByPlayerStatus[uuid] == null) {
            setStatus(uuid, PlayerStatus.ALIVE)
        }
        return uuidByPlayerStatus[uuid]!!
    }

}