package com.github.donghune.dimgame.repository.participant

import com.github.donghune.dimgame.manager.ParticipantStatus
import org.bukkit.entity.Player

var Player.participantStatus: ParticipantStatus
    get() {
        return ParticipantStatusRepository.getStatus(uniqueId)
    }
    set(value) {
        ParticipantStatusRepository.setStatus(uniqueId, value)
    }