package com.github.donghune.dimgame.repository.participant

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class ParticipantStatusRepository : AbstractParticipantRepository() {

    private val uuidByParticipantStatus: MutableMap<UUID, ParticipantStatus> = mutableMapOf()

    override fun setStatus(uuid: UUID, state: ParticipantStatus) {
        uuidByParticipantStatus[uuid] = state
    }

    override fun getStatus(uuid: UUID): ParticipantStatus {
        if (uuidByParticipantStatus[uuid] == null) {
            setStatus(uuid, ParticipantStatus.PARTICIPANT)
        }
        return uuidByParticipantStatus[uuid]!!
    }

    override fun getObserverList(): List<Player> {
        return Bukkit.getOnlinePlayers().filter { getStatus(it.uniqueId) == ParticipantStatus.OBSERVER }
    }

    override fun getParticipantList(): List<Player> {
        return Bukkit.getOnlinePlayers().filter { getStatus(it.uniqueId) == ParticipantStatus.PARTICIPANT }
    }

}