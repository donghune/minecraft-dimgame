package com.github.donghune.dimgame.repository.ingame

import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import org.bukkit.entity.Player

var Player.miniGameStatus: PlayerMiniGameStatus
    get() {
        return PlayerMiniGameStatusRepository.getStatus(uniqueId)
    }
    set(value) {
        PlayerMiniGameStatusRepository.setStatus(uniqueId, value)
    }