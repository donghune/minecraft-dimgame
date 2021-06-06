package com.github.donghune.dimgame.minigame.score_of_push


import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.dimgame.util.syncTeleport


import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox
import java.util.*

class ScoreOfPush : MiniGame<ScoreOfPushItem, ScoreOfPushScheduler>(
    name = ChatColor.DARK_AQUA.toString() + "점수 얻기 ( 밀치기 )",
    description = "빨간색 원 위에서 점수를 최대한 많이 얻으세요",
    mapLocations = MiniGameMap(
        BoundingBox(273.0, 99.0, 20.0, 346.0, 60.0, -63.0),
        Location(Bukkit.getWorld("world"), 311.5, 86.5, -21.5),
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = true,
        isChat = true
    )
) {

    override val gameItems: ScoreOfPushItem = ScoreOfPushItem()
    override val gameSchedulers: ScoreOfPushScheduler = ScoreOfPushScheduler(this)
    override val bossBar = Bukkit.createBossBar("남은시간 %02d:%02d", BarColor.BLUE, BarStyle.SOLID)

    internal val uuidByScore = mutableMapOf<UUID, Int>()
    internal val playTime = 60 * 1 - 1

    override suspend fun onStart() {
        Bukkit.getOnlinePlayers().forEach {
            it.inventory.addItem(gameItems.getItemById(ScoreOfPushItem.Code.STICK))
        }
        gameSchedulers.getScheduler(ScoreOfPushScheduler.Code.SCORE).runTick(1, Int.MAX_VALUE)
        gameSchedulers.getScheduler(ScoreOfPushScheduler.Code.MAIN).runSecond(1, playTime)
    }

    override suspend fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player
        player.syncTeleport(mapLocations.respawn)
        player.miniGameStatus = PlayerMiniGameStatus.ALIVE
    }

    override suspend fun gameStopCondition(): Boolean {
        return false
    }

}