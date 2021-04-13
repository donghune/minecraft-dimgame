package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.dimgame.entity.MiniGameState
import com.namu.dimgame.entity.PlayerState
import com.namu.namulibrary.schedular.SchedulerManager

fun mapScheduler(dimGame: DimGame) = SchedulerManager {
    doing {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapLocations.isIn(it)) {
                return@forEach
            }

            if (dimGame.miniGameState == MiniGameState.WAITING) {
                return@forEach
            }

            dimGame.setPlayerState(it, PlayerState.DIE)
        }
    }
}