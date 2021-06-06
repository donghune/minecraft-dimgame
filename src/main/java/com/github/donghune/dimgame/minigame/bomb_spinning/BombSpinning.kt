package com.github.donghune.dimgame.minigame.bomb_spinning

import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.minigame.resource.PotionsEffects
import com.github.donghune.dimgame.util.info
import com.github.donghune.dimgame.util.syncGameMode
import com.github.donghune.dimgame.util.syncTeleport
import com.github.donghune.namulibrary.extension.sendInfoMessage
import net.kyori.adventure.text.Component


import org.bukkit.*
import org.bukkit.block.data.type.TNT
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*

class BombSpinning : MiniGame<BombSpinningItems, BombSpinningSchedulers>(
    name = ChatColor.DARK_AQUA.toString() + "폭탄돌리기",
    description = "폭탄을 피하세요",
    mapLocations = MiniGameMap(
        BoundingBox(403.0, 101.0, 220.0, 379.0, 82.0, 244.0),
        Location(Bukkit.getWorld("world"), 391.0, 86.0, 232.0),
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false,
        isChat = true
    )
) {

    override val gameItems: BombSpinningItems = BombSpinningItems()
    override val gameSchedulers: BombSpinningSchedulers = BombSpinningSchedulers(this)
    override val bossBar: BossBar = Bukkit.createBossBar("B O O M", BarColor.RED, BarStyle.SOLID)

    private val finishedPlayerList = mutableListOf<UUID>()

    override suspend fun onStart() {
        gameSchedulers.getScheduler(BombSpinningSchedulers.Code.SET_BOMB_MAN).runSecond(1, 1)
    }

    override suspend fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        Bukkit.getOnlinePlayers().forEach {
            bossBar.removePlayer(it)
            it.activePotionEffects.forEach { potionEffect ->
                it.removePotionEffect(potionEffect.type)
            }
        }
    }

    @EventHandler
    suspend fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player

        player.syncGameMode(GameMode.SPECTATOR)
        player.syncTeleport(mapLocations.respawn)

        finishedPlayerList.add(player.uniqueId)
        Bukkit.broadcast(Component.text(info("${player.name}님이 탈락하셨습니다.")))

        if (gameStopCondition()) {
            finishedPlayerList.add(alivePlayers[0].uniqueId)
            stopGame(finishedPlayerList.apply { reverse() }.map { Bukkit.getPlayer(it)!! }.toList())
        }
    }

    override suspend fun gameStopCondition(): Boolean {
        return alivePlayers.size == 1
    }

    @EventHandler
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        val attacker = event.player
        val victim = event.rightClicked as? Player ?: return

        if (!attacker.isBombMan()) {
            println("${attacker.name} is not bomb man")
            return
        }

        attacker.releaseBombMan()
        victim.setBombMan()
    }

    fun setRandomBombMan() {
        alivePlayers.random().setBombMan()
    }

    fun getBombMan(): Player? {
        return alivePlayers.find { it.isBombMan() }
    }

    private fun Player.isBombMan(): Boolean {
        return inventory.helmet != null && inventory.helmet!!.type == Material.TNT
    }

    private fun Player.setBombMan() {
        world.playSound(location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
        inventory.contents = arrayOfNulls<ItemStack>(36).apply { fill(ItemStack(Material.TNT)) }
        inventory.helmet = ItemStack(Material.TNT)
        addPotionEffect(PotionsEffects.SLOW_20_2)
        addPotionEffect(PotionsEffects.BLINDNESS_40_1)
    }

    private fun Player.releaseBombMan() {
        activePotionEffects.forEach { removePotionEffect(it.type) }
        inventory.helmet = null
        inventory.clear()
    }

}

