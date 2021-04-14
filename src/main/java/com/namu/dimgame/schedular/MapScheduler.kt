package com.namu.dimgame.schedular

import com.namu.dimgame.minigame.DimGame
import com.namu.dimgame.minigame.MiniGameStatus
import com.namu.dimgame.minigame.PlayerStatus
import com.namu.namulibrary.schedular.SchedulerManager

fun mapScheduler(dimGame: DimGame<*, *>) = SchedulerManager {
    doing {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapLocations.isIn(it)) {
                return@forEach
            }

            if (dimGame.gameStatus == MiniGameStatus.WAITING) {
                return@forEach
            }

            dimGame.playerGameStatusManager.setStatus(it.uniqueId, PlayerStatus.DIE)
        }
    }
}