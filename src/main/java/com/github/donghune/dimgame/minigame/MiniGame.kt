package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.manager.RoundGameStatus
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.repository.ingame.PlayerMiniGameStatusRepository
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.dimgame.repository.other.ParticleResources
import com.github.donghune.namulibrary.schedular.SchedulerManager
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

abstract class MiniGame<ITEM : DimGameItem<*>, SCHEDULER : DimGameScheduler<*>>(
    val name: String,
    val description: String,
    val mapLocations: DimGameMap,
    val gameOption: DimGameOption,
) : Listener {

    abstract val gameItems: ITEM
    abstract val gameSchedulers: SCHEDULER

    open val bossBar: BossBar = Bukkit.createBossBar(description, BarColor.RED, BarStyle.SOLID)

    internal var gameStatus: RoundGameStatus = RoundGameStatus.WAITING
    private var observerPlayerList: List<Player> = listOf()
    internal var participationPlayerList: MutableList<Player> = mutableListOf()

    private lateinit var mapScheduler: SchedulerManager
    private lateinit var onMiniGameStopCallback: (List<Player>) -> Unit

    val alivePlayers
        get() = participationPlayerList.filter { it.miniGameStatus == PlayerMiniGameStatus.ALIVE }

    fun skipGame() {
        stopGame(participationPlayerList)
    }

    fun startGame(
        participationPlayerList: List<Player>,
        observerPlayerList: List<Player>,
        onMiniGameStopCallback: (List<Player>) -> Unit = {},
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
            it.miniGameStatus = PlayerMiniGameStatus.ALIVE
            bossBar.addPlayer(it)
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

        Bukkit.getPluginManager().registerSuspendingEvents(this, plugin)
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

        bossBar.removeAll()

        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.SPECTATOR
            it.inventory.clear()
        }

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            ParticleResources.executeMVPParticle(mapLocations.respawn)
            Bukkit.getOnlinePlayers().forEach {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle("1st", rank[0].name, 10, 60, 10)
                }
                rank.forEachIndexed { index, player ->
                    it.sendMessage("[DimGame] $index. ${player.name}")
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
        PlayerMiniGameStatusRepository.removeStatus(event.player.uniqueId)
    }

}
