package com.namu.dimgame.minigame

import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.manager.RoundGameStatus
import com.namu.dimgame.plugin
import com.namu.dimgame.repository.other.ParticleResources
import com.namu.namulibrary.extension.sendDebugMessage
import com.namu.namulibrary.particle.ParticleManager
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

abstract class DimGame<ITEM : DimGameItem<*>, SCHEDULER : DimGameScheduler<*>> : Listener {

    abstract val name: String
    abstract val description: String
    abstract val mapLocations: DimGameMap
    abstract val gameOption: DimGameOption
    abstract val defaultItems: List<ItemStack>
    abstract val gameItems: ITEM
    abstract val gameSchedulers: SCHEDULER

    internal var gameStatus: RoundGameStatus = RoundGameStatus.WAITING
    private var observerPlayerList: List<Player> = listOf()
    internal var participationPlayerList: MutableList<Player> = mutableListOf()

    internal val playerGameStatusManager = PlayerGameStatusManager(this)
    private lateinit var mapScheduler: SchedulerManager
    private lateinit var onMiniGameStopCallback: (List<Player>) -> Unit

    val alivePlayers
        get() = participationPlayerList.filter { playerGameStatusManager.getStatus(it.uniqueId) == PlayerStatus.ALIVE }

    fun skipGame() {
        stopGame(participationPlayerList)
    }

    fun startGame(
        participationPlayerList: List<Player>,
        observerPlayerList: List<Player>,
        onMiniGameStopCallback: (List<Player>) -> Unit = {}
    ) {
        // 전역 변수 등록
        this.onMiniGameStopCallback = onMiniGameStopCallback
        this.participationPlayerList = participationPlayerList.toMutableList()
        this.observerPlayerList = observerPlayerList

        // 게임상태 변경
        gameStatus = RoundGameStatus.RUNNING

        // 게임 옵션 등록 ( 이벤트 )
        this.gameOption.register()
        this.gameItems.register()

        // 참가자 리스폰으로 텔레포트 및 게임모드, 인벤토리 설정
        this.participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.SURVIVAL
            it.inventory.clear()
            it.inventory.addItem(*defaultItems.toTypedArray())
            playerGameStatusManager.setStatus(it.uniqueId, PlayerStatus.ALIVE)
        }

        // 옵저버도 동일하게 텔레포트
        this.observerPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.SPECTATOR
        }

        // 맵 스케쥴러 작동
        mapScheduler = mapScheduler(this).also {
            it.runTick(1, Int.MAX_VALUE)
        }

        onStart()

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun stopGame(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        EntityDamageEvent.getHandlerList().unregister(this)

        gameStatus = RoundGameStatus.WAITING

        // 각종 스케쥴러 스탑
        mapScheduler.stopScheduler()
        gameSchedulers.clearScheduler()

        // 이벤트 등록 해지
        this.gameOption.unregister()
        this.gameItems.unregister()

        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.SPECTATOR
            it.inventory.clear()
        }

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            ParticleResources.executeMVPParticle(mapLocations.respawn)
            Bukkit.getOnlinePlayers().forEach {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle("1st", rank[0].displayName, 10, 60, 10)
                }
                rank.forEachIndexed { index, player ->
                    it.sendMessage("[DimGame] $index. ${player.displayName}")
                }
            }
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                onStop(rank)
                onMiniGameStopCallback.invoke(rank)
            }, 60L)
        }, 20L)
    }

    abstract fun onStart()
    abstract fun onStop(rank: List<Player>)
    abstract fun onChangedPlayerState(player: Player, playerState: PlayerStatus)
    abstract fun gameStopCondition(): Boolean

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        participationPlayerList.removeIf { it.uniqueId == event.player.uniqueId }
        playerGameStatusManager.removeStatus(event.player.uniqueId)
    }

}
