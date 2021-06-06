package com.github.donghune.dimgame.util

import com.github.donghune.dimgame.plugin
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.minecraftDispatcher
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

fun Player.syncGameMode(value: GameMode) {
    plugin.launch(plugin.minecraftDispatcher) {
        println("Player.syncGameMode" + this.coroutineContext)
        println("Player.syncGameMode" + this.coroutineContext.javaClass.simpleName)
        gameMode = value
    }
}

fun Entity.syncTeleport(location: Location) {
    plugin.launch(plugin.minecraftDispatcher) {
        println("Entity.syncTeleport" + this.coroutineContext)
        println("Entity.syncTeleport" + this.coroutineContext.javaClass.simpleName)
        teleport(location)
    }
}

fun Entity.syncTeleport(entity: Entity) {
    plugin.launch(plugin.minecraftDispatcher) {
        println("Entity.syncTeleport" + this.coroutineContext)
        println("Entity.syncTeleport" + this.coroutineContext.javaClass.simpleName)
        teleport(entity)
    }
}