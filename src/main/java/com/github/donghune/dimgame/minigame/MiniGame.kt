package com.github.donghune.dimgame.minigame

import com.github.donghune.dimgame.events.MiniGameEndEvent
import com.github.donghune.dimgame.events.PlayerMiniGameAliveEvent
import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.manager.RoundGameStatus
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.repository.ingame.PlayerMiniGameStatusRepository
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.dimgame.repository.other.ParticleResources
import com.github.donghune.dimgame.util.broadcastOnTitle
import com.github.donghune.dimgame.util.info
import com.github.donghune.dimgame.util.syncGameMode
import com.github.donghune.dimgame.util.syncTeleport
import com.github.donghune.namulibrary.schedular.SchedulerManager
import com.github.shynixn.mccoroutine.callSuspendingEvent
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
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

abstract class MiniGame<ITEM : MiniGameItem<*>, SCHEDULER : MiniGameScheduler<*>>(
    val name: String,
    val description: String,
    val mapLocations: MiniGameMap,
    val gameOption: MiniGameOption,
) : Listener {

    abstract val gameItems: ITEM
    abstract val gameSchedulers: SCHEDULER

    open val bossBar: BossBar = Bukkit.createBossBar(description, BarColor.RED, BarStyle.SOLID)

    internal var gameStatus: RoundGameStatus = RoundGameStatus.WAITING
    private var observerPlayerList: List<Player> = listOf()
    internal var participationPlayerList: MutableList<Player> = mutableListOf()

    private lateinit var mapScheduler: SchedulerManager

    val alivePlayers
        get() = participationPlayerList.filter { it.miniGameStatus == PlayerMiniGameStatus.ALIVE }

    suspend fun skipGame() {
        stopGame(participationPlayerList)
    }

    suspend fun startGame(
        participationPlayerList: List<Player>,
        observerPlayerList: List<Player>,
    ) {

        // 전역 변수 등록
        this.participationPlayerList = participationPlayerList.toMutableList()
        this.observerPlayerList = observerPlayerList

        // 게임상태 변경
        gameStatus = RoundGameStatus.RUNNING

        // 게임 옵션 등록 ( 이벤트 )
        gameOption.register()
        gameItems.register()

        // 참가자 리스폰으로 텔레포트 및 게임모드, 인벤토리 설정
        participationPlayerList.forEach {
            it.syncTeleport(mapLocations.respawn)
            it.syncGameMode(GameMode.ADVENTURE)
            it.inventory.clear()
            it.miniGameStatus = PlayerMiniGameStatus.ALIVE
            bossBar.addPlayer(it)
        }

        // 옵저버도 동일하게 텔레포트
        observerPlayerList.forEach {
            it.syncTeleport(mapLocations.respawn)
            it.syncGameMode(GameMode.SPECTATOR)
        }

        // 맵 스케쥴러 작동
        mapScheduler = mapScheduler(this@MiniGame).also {
            it.runTick(1, Int.MAX_VALUE)
        }

        Bukkit.getPluginManager().registerSuspendingEvents(this@MiniGame, plugin)

        onStart()
    }

    suspend fun stopGame(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        EntityDamageEvent.getHandlerList().unregister(this)
        PlayerMiniGameDieEvent.getHandlerList().unregister(this)
        PlayerMiniGameAliveEvent.getHandlerList().unregister(this)

        gameStatus = RoundGameStatus.WAITING

        // 각종 스케쥴러 스탑
        mapScheduler.stopScheduler()
        gameSchedulers.clearScheduler()

        // 이벤트 등록 해지
        this.gameOption.unregister()
        this.gameItems.unregister()

        bossBar.removeAll()

        Bukkit.getOnlinePlayers().forEach {
            it.syncGameMode(GameMode.SPECTATOR)
            it.inventory.clear()
        }

        ParticleResources.executeMVPParticle(mapLocations.respawn)

        delay(1000L)

        broadcastOnTitle("1st", rank[0].name, 10, 60, 10)
        rank.forEachIndexed { index, player ->
            Bukkit.broadcast(Component.text(info("[DimGame] ${index + 1}. ${player.name}")))
        }

        delay(3000L)

        onStop(rank)
        Bukkit.getPluginManager().callSuspendingEvent(MiniGameEndEvent(rank.map { it.uniqueId }), plugin)

    }

    abstract suspend fun onStart()
    abstract suspend fun onStop(rank: List<Player>)
    abstract suspend fun gameStopCondition(): Boolean

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
