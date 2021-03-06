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

        // ?????? ?????? ??????
        this.participationPlayerList = participationPlayerList.toMutableList()
        this.observerPlayerList = observerPlayerList

        // ???????????? ??????
        gameStatus = RoundGameStatus.RUNNING

        // ?????? ?????? ?????? ( ????????? )
        gameOption.register()
        gameItems.register()

        // ????????? ??????????????? ???????????? ??? ????????????, ???????????? ??????
        participationPlayerList.forEach {
            it.syncTeleport(mapLocations.respawn)
            it.syncGameMode(GameMode.ADVENTURE)
            it.inventory.clear()
            it.miniGameStatus = PlayerMiniGameStatus.ALIVE
            bossBar.addPlayer(it)
        }

        // ???????????? ???????????? ????????????
        observerPlayerList.forEach {
            it.syncTeleport(mapLocations.respawn)
            it.syncGameMode(GameMode.SPECTATOR)
        }

        // ??? ???????????? ??????
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

        // ?????? ???????????? ??????
        mapScheduler.stopScheduler()
        gameSchedulers.clearScheduler()

        // ????????? ?????? ??????
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
