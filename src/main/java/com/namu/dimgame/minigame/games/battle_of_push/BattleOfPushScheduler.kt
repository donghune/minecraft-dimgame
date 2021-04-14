package com.namu.dimgame.minigame.games.battle_of_push

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.namulibrary.schedular.SchedulerManager

class BattleOfPushScheduler(
    dimGame: BattleOfPush
) : DimGameScheduler<BattleOfPushScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            doing {
                dimGame.participationPlayerList.forEach {
                    it.inventory.addItem(dimGame.gameItems.getItemList().random())
                }
            }
        }.registerScheduler(Code.RANDOM_ITEM)
    }

    enum class Code {
        RANDOM_ITEM
    }

}