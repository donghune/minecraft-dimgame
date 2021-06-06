package com.github.donghune.dimgame.minigame.to_avoid_anvil


import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.dimgame.util.*
import com.github.donghune.namulibrary.extension.addY
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
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
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.*

class ToAvoidAnvil : MiniGame<ToAvoidAnvilItem, ToAvoidAnvilScheduler>(
    name = ChatColor.DARK_GREEN.toString() + "모루피하기",
    description = "떨어지는 모루를 피하세요",
    mapLocations = MiniGameMap(
        BoundingBox(131.0, 103.0, 144.0, 105.0, 83.0, 170.0),
        Location(Bukkit.getWorld("world"), 118.0, 84.0, 157.0),
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false,
        isChat = true
    )
) {

    override val gameItems: ToAvoidAnvilItem = ToAvoidAnvilItem()
    override val gameSchedulers: ToAvoidAnvilScheduler = ToAvoidAnvilScheduler(this)

    override suspend fun onStart() {
        clearMap()
        delay(2000L)
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).runSecond(1L, Int.MAX_VALUE)
        delay(6000L)
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.PATTERN).runSecond(10L, Int.MAX_VALUE)
    }

    override suspend fun onStop(rank: List<Player>) {
        EntityDamageByBlockEvent.getHandlerList().unregister(this)
        EntityChangeBlockEvent.getHandlerList().unregister(this)
        tntEntityList.forEach { it.remove() }
        ExplosionPrimeEvent.getHandlerList().unregister(this)
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.MAIN).stopScheduler()
        gameSchedulers.getScheduler(ToAvoidAnvilScheduler.Code.PATTERN).stopScheduler()
    }

    private val finishedPlayerList = mutableListOf<UUID>()
    private val tntEntityList = mutableListOf<Entity>()

    @EventHandler
    suspend fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player

        player.syncGameMode(GameMode.SPECTATOR)
        player.syncTeleport(mapLocations.respawn)

        Bukkit.broadcast(Component.text(info("${player.name}님이 탈락하셨습니다.")))
        finishedPlayerList.add(player.uniqueId)

        if (alivePlayers.size == 1) {
            finishedPlayerList.add(alivePlayers[0].uniqueId)
            stopGame(finishedPlayerList.reversed().mapNotNull { Bukkit.getPlayer(it) }.toList())
        }
    }

    @EventHandler
    suspend fun onEntityChangeBlockEvent(event: EntityChangeBlockEvent) {
        if (event.entity.type != EntityType.FALLING_BLOCK) {
            return
        }

        event.isCancelled = true
        event.entity.remove()

        when ((event.entity as FallingBlock).blockData.material) {
            Material.ANVIL -> onGroundAnvil(event.block)
            Material.TNT -> onGroundTnt(event.block)
            else -> return
        }
    }

    @EventHandler
    suspend fun onExplosionPrimeEvent(event: ExplosionPrimeEvent) {
        event.isCancelled = true
        getNearByBlockList(event.entity.location)
            .onEach { it.world.playSound(it.location, Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 1f) }
            .forEach { underBlock -> damageToTheBlock(underBlock) }
    }

    private suspend fun onGroundAnvil(block: Block) {
        val world = block.world
        world.playSound(block.location, Sound.BLOCK_ANVIL_PLACE, 0.1f, 1f)
        world.getNearbyEntities(block.location, 0.5, 0.5, 0.5)
            .filterIsInstance<Player>()
            .firstOrNull { it.miniGameStatus == PlayerMiniGameStatus.ALIVE }
            ?.let { notNullPlayer -> notNullPlayer.miniGameStatus = PlayerMiniGameStatus.DIE }
        damageToTheBlock(block.location.addY(-1.0).block)
    }

    private fun onGroundTnt(block: Block) {
        tntEntityList.add(block.world.spawnEntity(block.location, EntityType.PRIMED_TNT))
    }

    private suspend fun damageToTheBlock(block: Block) {
        when (block.type) {
            Material.LIGHT_GRAY_WOOL -> block.type = Material.YELLOW_WOOL
            Material.YELLOW_WOOL -> block.type = Material.ORANGE_WOOL
            Material.ORANGE_WOOL -> {
                block.type = Material.RED_WOOL
                delay(1000L)
                block.type = Material.AIR
            }
            else -> Unit
        }
    }

    override suspend fun gameStopCondition(): Boolean {
        return true
    }

    internal fun spawnAnvil() {
        mapLocations.area
            .getTop()
            .toRandomLocation()
            .also { it.world.spawnFallingBlock(it, Material.ANVIL.createBlockData()) }
    }

    private fun clearMap() {
        mapLocations.area
            .getBlocks(Bukkit.getWorld("world")!!)
            .forEach { it.block.type = Material.AIR }

        mapLocations.area
            .getBottom()
            .getBlocks(Bukkit.getWorld("world")!!)
            .forEach { it.block.type = Material.LIGHT_GRAY_WOOL }
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

}