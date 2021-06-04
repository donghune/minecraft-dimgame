package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.manager.RoundGameStatus
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.namulibrary.schedular.SchedulerManager

fun mapScheduler(dimGame: MiniGame<*, *>) = SchedulerManager {
    doing {
        dimGame.participationPlayerList.forEach {
            if (dimGame.mapLocations.isIn(it)) {
                return@forEach
            }

            if (dimGame.gameStatus == RoundGameStatus.WAITING) {
                return@forEach
            }

            it.miniGameStatus = PlayerMiniGameStatus.DIE
        }
    }
}