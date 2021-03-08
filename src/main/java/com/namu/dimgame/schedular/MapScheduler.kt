package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.dimgame.entity.PlayerState
import com.namu.namulibrary.schedular.SchedulerManager

class MapScheduler(val dimGame: DimGame) : SchedulerManager() {
    override fun doing() {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapInfo.isIn(it)) {
                return@forEach
            }

            dimGame.setPlayerState(it, PlayerState.DIE)
        }
    }

    override fun finished() {
    }

    override fun started() {
    }
}