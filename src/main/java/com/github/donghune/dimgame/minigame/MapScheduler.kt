package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.manager.PlayerStatus
import com.github.donghune.dimgame.manager.RoundGameStatus
import com.github.donghune.namulibrary.schedular.SchedulerManager

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