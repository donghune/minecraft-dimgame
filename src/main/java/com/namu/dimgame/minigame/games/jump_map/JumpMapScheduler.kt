package com.namu.dimgame.minigame.games.jump_map

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.namulibrary.schedular.SchedulerManager

class JumpMapScheduler(dimGame: JumpMap) : DimGameScheduler<JumpMapScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            doing {
                dimGame.participationPlayerList.forEach {
                    it.inventory.addItem(dimGame.gameItems.getActionItemList().random())
                }
            }
        }.registerScheduler(Code.RANDOM_ITEM)
    }

    enum class Code {
        RANDOM_ITEM
    }
}