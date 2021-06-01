package com.namu.dimgame.util

import com.github.namu0240.namulibrary.schedular.SchedulerManager
import org.bukkit.entity.Player

fun Player.clearChat() {
    (1..20).forEach { _ ->
        sendMessage("")
    }
}

fun SchedulerManager.stopSchedulerNotWithFinish() {
    doingBukkitTask.cancel()
    cancelBukkitTask.cancel()
}