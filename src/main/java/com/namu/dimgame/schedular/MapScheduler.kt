package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.dimgame.entity.MiniGameState
import com.namu.dimgame.entity.PlayerState
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit

class MapScheduler(val dimGame: DimGame) : SchedulerManager() {
    override fun doing() {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapInfo.isIn(it)) {
                return@forEach
            }

            if (dimGame.miniGameState == MiniGameState.WAITING) {
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