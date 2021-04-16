package com.namu.dimgame.util

import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

fun Player.clearChat() {
    (1..20).forEach { _ ->
        sendMessage("")
    }
}

fun SchedulerManager.stopSchedulerNotWithFinish() {
    doingBukkitTask.cancel()
    cancelBukkitTask.cancel()
}