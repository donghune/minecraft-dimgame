package com.namu.dimgame.game

import com.namu.dimgame.entity.*
import com.namu.dimgame.plugin
import com.namu.namulibrary.schedular.SchedulerManager
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
import kotlin.random.Random

class BombSpinning : DimGame() {
    override val name: String = "폭탄돌리기"
    override val description: String = "폭탄을 피하세요"
    override val gameType: GameType = GameType.RANK
    override val mapLocations: GameMap = GameMap(
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

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)

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

    override fun onChangedPlayerState(player: Player, playerState: PlayerState) {
        when (playerState) {
            PlayerState.ALIVE -> {

            }
            PlayerState.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)

                finishedPlayerList.add(player.uniqueId)

                if (alivePlayers.size == 1) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopMiniGame(finishedPlayerList.apply { reverse() }.map { Bukkit.getPlayer(it)!! }.toList())
                }
            }
        }
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

    private fun Player.isBombMan(): Boolean {
        return (inventory.helmet?.type ?: Material.AIR) == Material.TNT
    }

    private val bossBar = Bukkit.createBossBar("B O O M", BarColor.RED, BarStyle.SOLID)

    private val bombManScheduler = SchedulerManager {
        doing { currentCycle ->
            if (cycle - currentCycle > 5) {
                return@doing
            }

            val bombMan = alivePlayers.find { it.isBombMan() } ?: return@doing

            bombMan.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SPEED,
                    Int.MAX_VALUE,
                    1,
                    true,
                    false,
                    true
                )
            )

            SchedulerManager {
                doing {
                    bombMan.world.spawnParticle(Particle.LAVA, bombMan.location.add(0.0, 2.0, 0.0), 5)
                }
            }.runTick(2, 10)
        }
        finished {
            val bombMan = alivePlayers.find { it.isBombMan() } ?: return@finished

            bombMan.world.createExplosion(bombMan.location, 1f, false, false)

            val nearPlayer = bombMan.getNearbyEntities(1.0, 1.0, 1.0)
                .filterIsInstance<Player>()
                .firstOrNull { it.gameMode != GameMode.SPECTATOR }

            setPlayerState(bombMan, PlayerState.DIE)
            if (nearPlayer != null) {
                setPlayerState(nearPlayer, PlayerState.DIE)
            }

            if (alivePlayers.count() >= 2) {
                setRandomBombMan()
            }
        }
    }

    private fun setRandomBombMan() {
        SchedulerManager {
            finished {
                alivePlayers.random().setBombMan()
                bombManScheduler.runSecond(1, Random.nextInt(15, 20))
            }
        }.runSecond(1, 2)
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