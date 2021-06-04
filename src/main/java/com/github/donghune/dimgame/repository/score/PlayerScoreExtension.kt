package com.github.donghune.dimgame.repository.score

import org.bukkit.entity.Player

var Player.score: Int
    get() {
        return PlayerScoreRepository.getPlayerScore(uniqueId)
    }
    set(value) {
        PlayerScoreRepository.setPlayerScore(uniqueId, value)
    }