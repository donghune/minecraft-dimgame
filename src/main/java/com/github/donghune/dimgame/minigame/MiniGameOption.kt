package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.plugin
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent

data class MiniGameOption(
    val isBlockPlace: Boolean,
    val isBlockBreak: Boolean,
    val isCraft: Boolean,
    val isAttack: Boolean,
    val isChat: Boolean,
) : Listener {

    fun register() {
        Bukkit.getPluginManager().registerSuspendingEvents(this, plugin)
    }

    fun unregister() {
        BlockPlaceEvent.getHandlerList().unregister(this)
        BlockBreakEvent.getHandlerList().unregister(this)
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)
        AsyncChatEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onOptionalBlockPlaceEvent(event: BlockPlaceEvent) {

        if (isBlockPlace) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onOptionalBlockBreakEvent(event: BlockBreakEvent) {

        if (isBlockBreak) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onOptionalEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {

        if (event.damager !is Player) {
            return
        }

        if (event.entity !is Player) {
            return
        }

        if (isAttack) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onOptionalCraftItemEvent(event: CraftItemEvent) {

        if (isCraft) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerChatEvent(event: AsyncChatEvent) {

        if (event.player.isOp) {
            return
        }

        if (isChat) {
            return
        }

        event.isCancelled = true
    }

}