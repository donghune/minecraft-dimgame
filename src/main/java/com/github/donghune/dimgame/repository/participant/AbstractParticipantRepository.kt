package com.github.donghune.dimgame.repository.participant

import org.bukkit.entity.Player
import java.util.*

abstract class AbstractParticipantRepository {

    abstract fun setStatus(uuid: UUID, state: ParticipantStatus)

    abstract fun getStatus(uuid: UUID): ParticipantStatus

    abstract fun getParticipantList() : List<Player>

    abstract fun getObserverList() : List<Player>

}