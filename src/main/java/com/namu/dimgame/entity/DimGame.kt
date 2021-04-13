package com.namu.dimgame.entity

import com.namu.dimgame.DimGameManager
import com.namu.dimgame.plugin
import com.namu.dimgame.schedular.mapScheduler
import com.namu.namulibrary.extension.sendDebugMessage
import com.namu.namulibrary.nms.addNBTTagCompound
import com.namu.namulibrary.nms.getNBTTagCompound
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class DimGame : Listener, DimGameListener {

    abstract val name: String
    abstract val description: String
    abstract val gameType: GameType
    abstract val mapLocations: GameMap
    abstract val gameOption: DimGameOption
    abstract val defaultItems: List<ItemStack>

    val gameItems: MutableList<ItemStack> = mutableListOf()
    var miniGameState: MiniGameState = MiniGameState.WAITING
    var observerPlayerList: List<Player> = listOf()
    var participationPlayerList: MutableList<Player> = mutableListOf()
    private val stateOfPlayer: MutableMap<UUID, PlayerState> = mutableMapOf()

    private lateinit var mapScheduler: SchedulerManager
    private lateinit var onMiniGameStopCallback: () -> Unit

    private val gameActionItemMap = mutableMapOf<UUID, (PlayerInteractEvent) -> Unit>()

    val alivePlayers
        get() = participationPlayerList.filter { getPlayerState(it) == PlayerState.ALIVE }

    fun startMiniGame(
        participationPlayerList: List<Player>,
        observerPlayerList: List<Player>,
        onMiniGameStopCallback: () -> Unit = {}
    ) {
        this.onMiniGameStopCallback = onMiniGameStopCallback
        this.participationPlayerList = participationPlayerList.toMutableList()
        this.observerPlayerList = observerPlayerList

        this.gameOption.register()

        this.participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.SURVIVAL
            it.inventory.clear()
            it.inventory.addItem(*defaultItems.toTypedArray())
            setPlayerState(it, PlayerState.ALIVE)
        }

        this.observerPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.SPECTATOR
        }

        mapScheduler = mapScheduler(this)
        mapScheduler.runTick(1, Int.MAX_VALUE)

        miniGameState = MiniGameState.RUNNING

        Bukkit.getPluginManager().registerEvents(this, plugin)

        onStart()
    }

    fun stopMiniGame(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        EntityDamageEvent.getHandlerList().unregister(this)

        Bukkit.getOnlinePlayers().forEach {
            rank.forEachIndexed { index: Int, player: Player ->
                it.sendDebugMessage("${index + 1}. ${player.displayName}")
            }
        }

        miniGameState = MiniGameState.WAITING

        // 각종 스케쥴러 스탑
        mapScheduler.stopScheduler()

        // 각 게임별 개인 스탑 처리
        onStop(rank)

        // 이벤트 등록 해지
        gameOption.unregister()

        // 게임모드 변경 및 로비로 텔레포트
        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.ADVENTURE
            it.inventory.clear()
        }

        onMiniGameStopCallback.invoke()
    }

    fun setPlayerState(player: Player, playerState: PlayerState) {
        stateOfPlayer[player.uniqueId] = playerState
        onChangedPlayerState(player, playerState)
    }

    fun getPlayerState(player: Player): PlayerState {
        return getPlayerState(player.uniqueId)
    }

    fun getPlayerState(uuid: UUID): PlayerState {
        if (stateOfPlayer[uuid] == null) {
            stateOfPlayer[uuid] = PlayerState.ALIVE
        }
        return stateOfPlayer[uuid]!!
    }

    fun registerGameItem(
        itemStack: ItemStack,
        action: (PlayerInteractEvent) -> Unit
    ) {
        val uuid = UUID.randomUUID()
        val realItem = itemStack.addNBTTagCompound(uuid)
        gameItems.add(realItem)
        gameActionItemMap[uuid] = action
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
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

        gameActionItemMap[handItem.getNBTTagCompound(UUID::class.java)]?.let {
            event.isCancelled = true
            handItem.amount -= 1
            it.invoke(event)
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        participationPlayerList.removeIf { it.uniqueId == event.player.uniqueId }
        stateOfPlayer.remove(event.player.uniqueId)
    }

}
