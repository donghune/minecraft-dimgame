package com.namu.dimgame.minigame.battle_of_push

import com.namu.dimgame.minigame.DimGameScheduler
import com.github.namu0240.namulibrary.schedular.SchedulerManager

class BattleOfPushScheduler(
    dimGame: BattleOfPush
) : DimGameScheduler<BattleOfPushScheduler.Code>(dimGame) {

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