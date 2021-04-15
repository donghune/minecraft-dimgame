package com.namu.dimgame.minigame.games.bomb_spinning

import com.namu.dimgame.minigame.*
import com.namu.dimgame.plugin
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class BombSpinning : DimGame<BombSpinningItems, BombSpinningSchedulers>() {
    override val name: String = "폭탄돌리기"
    override val description: String = "폭탄을 피하세요"
    override val mapLocations: DimGameMap = DimGameMap(
        Location(Bukkit.getWorld("world"), 403.0, 101.0, 220.0),
        Location(Bukkit.getWorld("world"), 379.0, 82.0, 244.0),
        Location(Bukkit.getWorld("world"), 391.0, 86.0, 232.0),
    )
    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false
    )
    override val defaultItems: List<ItemStack> = emptyList()
    override val gameItems: BombSpinningItems = BombSpinningItems()
    override val gameSchedulers: BombSpinningSchedulers = BombSpinningSchedulers(this)

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOW_DIGGING,
                    Int.MAX_VALUE,
                    0,
                    false,
                    false,
                    false
                )
            )
            bossBar.addPlayer(it)
        }

        setRandomBombMan()
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        Bukkit.getOnlinePlayers().forEach {
            bossBar.removePlayer(it)
            it.activePotionEffects.forEach { potionEffect ->
                it.removePotionEffect(potionEffect.type)
            }
        }
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)

                finishedPlayerList.add(player.uniqueId)

                if (gameStopCondition()) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopGame(finishedPlayerList.apply { reverse() }.map { Bukkit.getPlayer(it)!! }.toList())
                }
            }
        }
    }

    override fun gameStopCondition(): Boolean {
        return alivePlayers.size == 1
    }

    @EventHandler
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        val attacker = event.player
        val victim = event.rightClicked as? Player ?: return

        if (!attacker.isBombMan()) {
            return
        }

        attacker.releaseBombMan()
        victim.setBombMan()
    }

    private val bossBar = Bukkit.createBossBar("B O O M", BarColor.RED, BarStyle.SOLID)

    fun setRandomBombMan() {
        alivePlayers.random().setBombMan()
    }

    fun getBombMan(): Player? {
        return alivePlayers.find { it.isBombMan() }
    }

    private fun Player.isBombMan(): Boolean {
        return (inventory.helmet?.type ?: Material.AIR) == Material.TNT
    }

    private fun Player.setBombMan() {
        world.playSound(location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
        inventory.helmet = ItemStack(Material.TNT)
        (0 until 36).forEach { inventory.setItem(it, ItemStack(Material.TNT)) }
        PotionEffect(
            PotionEffectType.SLOW,
            20,
            2,
            true,
            false,
            true
        ).also { addPotionEffect(it) }
        PotionEffect(
            PotionEffectType.BLINDNESS,
            40,
            1,
            true,
            false,
            true
        ).also { addPotionEffect(it) }
    }

    private fun Player.releaseBombMan() {
        activePotionEffects.forEach { removePotionEffect(it.type) }
        inventory.helmet = null
        inventory.clear()
    }

}