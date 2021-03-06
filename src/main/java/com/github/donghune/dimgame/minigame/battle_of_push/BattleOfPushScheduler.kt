package com.github.donghune.dimgame.minigame.battle_of_push

import com.github.donghune.dimgame.minigame.MiniGameScheduler
import com.github.donghune.namulibrary.schedular.SchedulerManager

class BattleOfPushScheduler(
    dimGame: BattleOfPush
) : MiniGameScheduler<BattleOfPushScheduler.Code>(dimGame) {

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