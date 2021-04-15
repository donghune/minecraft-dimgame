package com.namu.dimgame.minigame.games.to_avoid_anvil


import com.namu.dimgame.minigame.*
import com.namu.namulibrary.extension.ItemBuilder
import com.namu.namulibrary.extension.replaceChatColorCode
import com.namu.namulibrary.schedular.SchedulerManager
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

class ToAvoidAnvil : DimGame<ToAvoidAnvilItem, ToAvoidAnvilScheduler>() {
    override val name: String = "모루피하기"
    override val description: String = "떨어지는 모루를 피하세요"

    override val mapLocations: DimGameMap = DimGameMap(
            Location(Bukkit.getWorld("world"), 131.0, 103.0, 144.0),
            Location(Bukkit.getWorld("world"), 105.0, 83.0, 170.0),
            Location(Bukkit.getWorld("world"), 118.0, 84.0, 157.0),
    )

    override val gameOption: DimGameOption = DimGameOption(
            isBlockPlace = false,
            isBlockBreak = false,
            isCraft = false,
            isAttack = false
    )
    override val gameItems: ToAvoidAnvilItem = ToAvoidAnvilItem()
    override val gameSchedulers: ToAvoidAnvilScheduler = ToAvoidAnvilScheduler(this)
    override val defaultItems: List<ItemStack> = emptyList()

    override fun onStart() {
        clearMap()
        participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.ADVENTURE
        }
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).runSecond(1L, Int.MAX_VALUE)
    }

    override fun onStop(rank: List<Player>) {
        EntityDamageByBlockEvent.getHandlerList().unregister(this)
        EntityChangeBlockEvent.getHandlerList().unregister(this)
        participationPlayerList.forEach {
            it.gameMode = GameMode.SURVIVAL
        }
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).stopScheduler()
    }

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)
                if (!finishedPlayerList.contains(player.uniqueId)) {
                    finishedPlayerList.add(player.uniqueId)
                }

                if (alivePlayers.size == 1) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopGame(finishedPlayerList.reversed().mapNotNull { Bukkit.getPlayer(it) }.toList())
                }
            }
        }
    }

    @EventHandler
    fun onEntityChangeBlockEvent(event: EntityChangeBlockEvent) {
        if (event.entity.type == EntityType.FALLING_BLOCK) {

            // entity
            event.isCancelled = true
            event.entity.remove()
            event.block.world.playSound(event.block.location, Sound.BLOCK_ANVIL_PLACE, 0.1f, 1f)

            // player
            val player: Player? = event.block.world.getNearbyEntities(event.block.location, 0.1, 0.1, 0.1)
                    .filter { playerGameStatusManager.getStatus(it.uniqueId) == PlayerStatus.ALIVE }
                    .find { it is Player } as? Player
            player?.let { notNullPlayer -> playerGameStatusManager.setStatus(notNullPlayer.uniqueId, PlayerStatus.DIE) }

            // change block info
            val underBlock: Block = event.block.location.apply { y-- }.block
            when (underBlock.type) {
                Material.LIGHT_GRAY_WOOL -> underBlock.type = Material.YELLOW_WOOL
                Material.YELLOW_WOOL -> underBlock.type = Material.ORANGE_WOOL
                Material.ORANGE_WOOL -> SchedulerManager {
                    started { underBlock.type = Material.RED_WOOL }
                    finished { underBlock.type = Material.AIR }
                }.runSecond(1L, 1)
                else -> Unit
            }
        }
    }

    override fun gameStopCondition(): Boolean {
        return true
    }

    internal fun spawnAnvil() {
        val randomLocation = Location(
                Bukkit.getWorld("world"),
                Random.nextInt(mapLocations.startX..mapLocations.endX) + 0.5,
                Random.nextInt(mapLocations.endY, mapLocations.endY + 1).toDouble(),
                Random.nextInt(mapLocations.startZ..mapLocations.endZ) + 0.5,
        )

        Bukkit.getWorld("world")?.spawnFallingBlock(
                randomLocation,
                Material.ANVIL.createBlockData()
        )
    }

    private fun clearMap() {
        for (x in mapLocations.startX..mapLocations.endX) {
            for (z in mapLocations.startZ..mapLocations.endZ) {
                Location(
                        mapLocations.pos1.world,
                        x.toDouble(),
                        mapLocations.startY.toDouble(),
                        z.toDouble()
                ).block.type = Material.LIGHT_GRAY_WOOL

                Location(
                        mapLocations.pos1.world,
                        x.toDouble(),
                        mapLocations.startY.toDouble() + 1,
                        z.toDouble()
                ).block.type = Material.AIR
            }
        }
    }

}