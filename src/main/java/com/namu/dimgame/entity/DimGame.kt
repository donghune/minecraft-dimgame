package com.namu.dimgame.entity

import com.namu.dimgame.plugin
import com.namu.dimgame.schedular.MapScheduler
import com.namu.dimgame.schedular.MiniGameScheduler
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

abstract class DimGame : Listener, DimGameListener {

    abstract val name: String
    abstract val description: String
    abstract val gameType: GameType
    abstract val mapInfo: GameMap
    abstract val gameOption: DimGameOption
    abstract val defaultItems: List<ItemStack>
    abstract val gameItems: List<ItemStack>

    var miniGameState : MiniGameState = MiniGameState.WAITING
    var observerPlayerList: List<Player> = listOf()
    var participationPlayerList: List<Player> = listOf()
    private val stateOfPlayer: MutableMap<UUID, PlayerState> = mutableMapOf()

    private lateinit var miniGameScheduler: MiniGameScheduler
    private lateinit var mapScheduler: MapScheduler
    private lateinit var onMiniGameStopCallback: () -> Unit

    fun startMiniGame(
            participationPlayerList: List<Player>,
            observerPlayerList: List<Player>,
            onMiniGameStopCallback: () -> Unit = {}
    ) {
        this.onMiniGameStopCallback = onMiniGameStopCallback
        this.participationPlayerList = participationPlayerList
        this.observerPlayerList = observerPlayerList

        this.gameOption.register()

        this.participationPlayerList.forEach {
            it.teleport(mapInfo.respawn)
            it.gameMode = GameMode.SURVIVAL
            it.inventory.clear()
            it.inventory.addItem(*defaultItems.toTypedArray())
            stateOfPlayer[it.uniqueId] = PlayerState.ALIVE
        }

        this.observerPlayerList.forEach {
            it.teleport(mapInfo.respawn)
            it.gameMode = GameMode.SPECTATOR
        }

        miniGameScheduler = MiniGameScheduler(this)
        miniGameScheduler.runSecond(1, 3)

        mapScheduler = MapScheduler(this)
        mapScheduler.runTick(1, Int.MAX_VALUE)

        miniGameState = MiniGameState.RUNNING
    }

    fun stopMiniGame(rank: List<Player>) {
        // 각 게임별 개인 스탑 처리
        onStop(rank)

        miniGameState = MiniGameState.WAITING

        // 이벤트 등록 해지
        gameOption.unregister()

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

}
