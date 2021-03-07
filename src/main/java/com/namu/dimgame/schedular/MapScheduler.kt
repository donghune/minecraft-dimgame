package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.namulibrary.schedular.SchedulerManager

class MapScheduler(val dimGame: DimGame) : SchedulerManager() {
    override fun doing() {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapInfo.isIn(it)) {
                return@forEach
            }

            dimGame.setPlayerState(it, DimGame.PlayerState.DIE)
        }
    }

    override fun finished() {
    }

    override fun started() {
    }
}