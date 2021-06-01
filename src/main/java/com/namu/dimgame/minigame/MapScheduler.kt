package com.namu.dimgame.minigame

import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.manager.RoundGameStatus
import com.github.namu0240.namulibrary.schedular.SchedulerManager

fun mapScheduler(dimGame: DimGame<*, *>) = SchedulerManager {
    doing {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapLocations.isIn(it)) {
                return@forEach
            }

            if (dimGame.gameStatus == RoundGameStatus.WAITING) {
                return@forEach
            }

            dimGame.playerGameStatusManager.setStatus(it.uniqueId, PlayerStatus.DIE)
        }
    }
}