package com.namu.dimgame.game

import java.util.*

class PlayerParticipantStatusManager {

    private val uuidByParticipantStatus: MutableMap<UUID, ParticipantStatus> = mutableMapOf()

    fun removeStatus(uuid : UUID) {
        uuidByParticipantStatus.remove(uuid)
    }

    fun setStatus(uuid: UUID, state: ParticipantStatus) {
        uuidByParticipantStatus[uuid] = state
    }

    fun getStatus(uuid: UUID): ParticipantStatus {
        if (uuidByParticipantStatus[uuid] == null) {
            setStatus(uuid, ParticipantStatus.PARTICIPANT)
        }
        return uuidByParticipantStatus[uuid]!!
    }

}