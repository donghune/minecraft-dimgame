package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit

class MiniGameScheduler(private val dimGame: DimGame) : SchedulerManager() {

    var countDown = 3

    override fun doing() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(countDown.toString(), "", 10, 20, 10)
        }
        countDown -= 1
    }

    override fun finished() {
        dimGame.onStart()
    }

    override fun started() {
        dimGame.onPrepare()
    }
}