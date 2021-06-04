package com.github.donghune.dimgame.minigame.jump_map

import com.github.donghune.dimgame.minigame.DimGameScheduler
import com.github.donghune.namulibrary.schedular.SchedulerManager

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