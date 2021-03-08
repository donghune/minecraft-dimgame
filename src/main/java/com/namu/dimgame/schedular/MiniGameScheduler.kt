package com.namu.dimgame.schedular

import com.namu.dimgame.entity.DimGame
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit

class MiniGameScheduler(
    val gameName: String,
    val onStart: () -> Unit,
    val onFinish: () -> Unit
) : SchedulerManager() {

    var countDown = 3

    override fun doing() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(gameName, countDown.toString() + "초 뒤 시작", 10, 20, 10)
        }
        countDown -= 1
    }

    override fun finished() {
        onFinish.invoke()
    }

    override fun started() {
        onStart.invoke()
    }
}