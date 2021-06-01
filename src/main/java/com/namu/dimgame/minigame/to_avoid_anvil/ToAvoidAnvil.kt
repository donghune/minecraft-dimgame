package com.namu.dimgame.minigame.to_avoid_anvil


import com.github.namu0240.namulibrary.extension.sendInfoMessage
import com.github.namu0240.namulibrary.schedular.SchedulerManager
import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.DimGame
import com.namu.dimgame.minigame.DimGameMap
import com.namu.dimgame.minigame.DimGameOption
import com.namu.dimgame.plugin
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

class ToAvoidAnvil : DimGame<ToAvoidAnvilItem, ToAvoidAnvilScheduler>() {
    override val name: String = ChatColor.DARK_GREEN.toString() + "모루피하기"
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
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).runSecond(1L, Int.MAX_VALUE)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.PATTERN).runSecond(10L, Int.MAX_VALUE)
            }, 110L)
        }, 60L)
    }

    override fun onStop(rank: List<Player>) {
        EntityDamageByBlockEvent.getHandlerList().unregister(this)
        EntityChangeBlockEvent.getHandlerList().unregister(this)
        tntEntityList.forEach { it.remove() }
        ExplosionPrimeEvent.getHandlerList().unregister(this)
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).stopScheduler()
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.PATTERN).stopScheduler()
    }

    private val finishedPlayerList = mutableListOf<UUID>()
    private val tntEntityList = mutableListOf<Entity>()

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)
                if (!finishedPlayerList.contains(player.uniqueId)) {
                    finishedPlayerList.add(player.uniqueId)
                    Bukkit.getOnlinePlayers().forEach { it.sendInfoMessage("${player.displayName}님이 탈락하셨습니다.") }
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

            when ((event.entity as FallingBlock).blockData.material) {
                Material.ANVIL -> {
                    event.block.world.playSound(event.block.location, Sound.BLOCK_ANVIL_PLACE, 0.1f, 1f)

                    // player
                    event.block.world.getNearbyEntities(event.block.location, 0.5, 0.5, 0.5)
                        .filterIsInstance<Player>()
                        .firstOrNull { playerGameStatusManager.getStatus(it.uniqueId) == PlayerStatus.ALIVE }
                        ?.let { notNullPlayer ->
                            playerGameStatusManager.setStatus(
                                notNullPlayer.uniqueId,
                                PlayerStatus.DIE
                            )
                        }

                    val underBlock: Block = event.block.location.apply { y-- }.block

                    // change block info
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
                Material.TNT -> {
                    tntEntityList.add(event.block.world.spawnEntity(event.block.location, EntityType.PRIMED_TNT))
                }
                else -> {
                    println(event.block.type)
                }
            }
        }
    }

    @EventHandler
    fun onExplosionPrimeEvent(event: ExplosionPrimeEvent) {
        event.isCancelled = true
        getNearByBlockList(event.entity.location)
            .onEach { it.world.playSound(it.location, Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 1f) }
            .forEach { underBlock ->
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

    private fun getNearByBlockList(location: Location): List<Block> {
        return listOf(
            Vector(1.0, -1.0, -1.0),
            Vector(1.0, -1.0, 0.0),
            Vector(1.0, -1.0, 1.0),
            Vector(0.0, -1.0, -1.0),
            Vector(0.0, -1.0, 0.0),
            Vector(0.0, -1.0, 1.0),
            Vector(-1.0, -1.0, -1.0),
            Vector(-1.0, -1.0, 0.0),
            Vector(-1.0, -1.0, 1.0),
        ).map {
            location.clone().add(it).block
        }.toList()
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

                repeat(100) {
                    Location(
                        mapLocations.pos1.world,
                        x.toDouble(),
                        mapLocations.startY.toDouble() + it + 1,
                        z.toDouble()
                    ).block.type = Material.AIR
                }
            }
        }
    }

}