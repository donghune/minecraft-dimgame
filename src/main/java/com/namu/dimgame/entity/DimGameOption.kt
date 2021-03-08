package com.namu.dimgame.entity

import com.namu.dimgame.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent

data class DimGameOption(
        val isBlockPlace: Boolean,
        val isBlockBreak: Boolean,
        val isCraft: Boolean,
        val isAttack: Boolean,
) : Listener {

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun unregister() {
        BlockPlaceEvent.getHandlerList().unregister(this)
        BlockBreakEvent.getHandlerList().unregister(this)
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)
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

}