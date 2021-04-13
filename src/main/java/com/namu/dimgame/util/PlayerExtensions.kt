package com.namu.dimgame.util

import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

fun Player.clearChat() {
    (1..20).forEach { _ ->
        sendMessage("")
    }
}
