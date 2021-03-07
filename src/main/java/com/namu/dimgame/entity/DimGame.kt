package com.namu.dimgame.entity

import com.namu.dimgame.schedular.MapScheduler
import com.namu.dimgame.schedular.MiniGameScheduler
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class DimGame : Listener {

    abstract val name: String
    abstract val description: String
    abstract val gameType: GameType
    abstract val mapInfo: GameMap

    abstract val isBlockPlace: Boolean
    abstract val isBlockBreak: Boolean
    abstract val isCraft: Boolean
    abstract val isAttack: Boolean

    abstract val defaultItems: List<ItemStack>
    abstract val gameItems: List<ItemStack>

    abstract fun onPrepare()
    abstract fun onStart()
    abstract fun onStop(rank: List<Player>)
    abstract fun onChangedPlayerState(player: Player)

    lateinit var participationPlayerList: List<Player>
    private val stateOfPlayer: MutableMap<UUID, PlayerState> = mutableMapOf()

    private lateinit var miniGameScheduler : MiniGameScheduler
    private lateinit var mapScheduler : MapScheduler
    private lateinit var onMiniGameStopCallback : () -> Unit

    fun startMiniGame(participationPlayerList: List<Player>, onMiniGameStopCallback : () -> Unit = {}) {
        this.onMiniGameStopCallback = onMiniGameStopCallback
        this.participationPlayerList = participationPlayerList

        this.participationPlayerList.forEach {
            it.teleport(mapInfo.respawn)
            it.inventory.clear()
            it.inventory.addItem(*defaultItems.toTypedArray())
            it.gameMode = GameMode.SURVIVAL
            stateOfPlayer[it.uniqueId] = PlayerState.ALIVE
        }

        miniGameScheduler = MiniGameScheduler(this)
        mapScheduler = MapScheduler(this)

        miniGameScheduler.runSecond(1, 3)
        mapScheduler.runTick(1, Int.MAX_VALUE)
    }

    fun stopMiniGame(rank: List<Player>) {
        // 각 게임별 개인 스탑 처리
        onStop(rank)

        // 이벤트 등록 해지
        BlockPlaceEvent.getHandlerList().unregister(this)
        BlockBreakEvent.getHandlerList().unregister(this)
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)

        // 각종 스케쥴러 스탑
        miniGameScheduler.stopScheduler()
        mapScheduler.stopScheduler()

        // 게임모드 변경 및 로비로 텔레포트
        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.ADVENTURE
            it.inventory.clear()
        }

        onMiniGameStopCallback.invoke()
    }

    fun setPlayerState(player: Player, playerState: PlayerState) {
        stateOfPlayer[player.uniqueId] = playerState
        onChangedPlayerState(player)
    }

    fun getPlayerState(player: Player): PlayerState {
        if (stateOfPlayer[player.uniqueId] == null) {
            stateOfPlayer[player.uniqueId] = PlayerState.ALIVE
        }
        return stateOfPlayer[player.uniqueId]!!
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

    enum class PlayerState {
        ALIVE, DIE;
    }

    enum class MiniGameState {
        WAITING, RUNNING
    }

}
