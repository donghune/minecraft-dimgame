package com.namu.dimgame.schedular

import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit

fun MiniGameScheduler(
    gameName: String,
    onStart: () -> Unit,
    onFinish: () -> Unit
) = SchedulerManager {

    var countDown = 5

    doing {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(gameName, countDown.toString() + "초 뒤 시작", 10, 20, 10)
        }
        countDown -= 1
    }

    finished {
        onFinish.invoke()
    }

    started {
        onStart.invoke()
    }
}