package com.namu.dimgame.minigame

import com.namu.dimgame.plugin
import com.namu.namulibrary.nms.addNBTTagCompound
import com.namu.namulibrary.nms.getNBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class DimGameItem<ID> : Listener {

    private val idByAction = mutableMapOf<ID, (PlayerInteractEvent) -> Unit>()
    private val idByItem = mutableMapOf<ID, ItemStack>()
    private val uuidById = mutableMapOf<UUID, ID>()

    fun getItemList(): List<ItemStack> {
        return idByItem.values.toList()
    }

    fun getItemById(id: ID): ItemStack {
        return idByItem[id]!!
    }

    fun ItemStack.registerAction(
        itemId: ID,
        action: (PlayerInteractEvent) -> Unit = {}
    ) {
        val uuid = UUID.randomUUID()
        addNBTTagCompound(uuid)
        idByItem[itemId] = this
        uuidById[uuid] = itemId
        idByAction[itemId] = action
    }

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun unregister() {
        PlayerInteractEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onPlayerInteractEventItem(event: PlayerInteractEvent) {
        val handItem = event.player.inventory.itemInMainHand

        if (handItem.type.isAir) {
            return
        }

        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        val id = uuidById[handItem.getNBTTagCompound(UUID::class.java)] ?: return

        idByAction[id]?.let {
            event.isCancelled = true
            handItem.amount -= 1
            it.invoke(event)
        }
    }

}