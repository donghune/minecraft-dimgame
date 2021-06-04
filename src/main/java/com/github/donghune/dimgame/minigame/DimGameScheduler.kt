package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.util.stopSchedulerNotWithFinish
import com.github.donghune.namulibrary.schedular.SchedulerManager

abstract class DimGameScheduler<ID>(
        protected val dimGame: DimGame<*, *>
) {

    private val idByScheduler = mutableMapOf<ID, SchedulerManager>()

    fun clearScheduler() {
        idByScheduler.values.forEach { it.stopSchedulerNotWithFinish() }
    }

    fun getScheduler(id: ID): SchedulerManager {
        return idByScheduler[id]!!
    }

    fun SchedulerManager.registerScheduler(id: ID) {
        idByScheduler[id] = this
    }

    fun deleteScheduler(id: ID) {
        idByScheduler[id]!!.stopScheduler()
        idByScheduler.remove(id)
    }

}