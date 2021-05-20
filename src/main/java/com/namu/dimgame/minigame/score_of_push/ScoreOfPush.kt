package com.namu.dimgame.minigame.score_of_push


import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random

class ScoreOfPush : DimGame<ScoreOfPushItem, ScoreOfPushScheduler>() {
    override val name: String = "점수 얻기 ( 밀치기 )"
    override val description: String = "빨간색 원 위에서 점수를 최대한 많이 얻으세요"
    override val mapLocations: DimGameMap = DimGameMap(
            Location(Bukkit.getWorld("world"), 273.0, 99.0, 20.0),
            Location(Bukkit.getWorld("world"), 346.0, 60.0, -63.0),
            Location(Bukkit.getWorld("world"), 311.5, 86.5, -21.5),
    )
    override val gameOption: DimGameOption = DimGameOption(
            isBlockPlace = false,
            isBlockBreak = false,
            isCraft = false,
            isAttack = true
    )

    override val gameItems: ScoreOfPushItem = ScoreOfPushItem()
    override val gameSchedulers: ScoreOfPushScheduler = ScoreOfPushScheduler(this)
    override val defaultItems: List<ItemStack> = listOf(
            gameItems.getItemById(ScoreOfPushItem.Code.STICK)
    )

    internal val uuidByScore = mutableMapOf<UUID, Int>()
    internal val bossBar = Bukkit.createBossBar("남은시간 %02d:%02d", BarColor.BLUE, BarStyle.SEGMENTED_20)
    internal val playTime = 60 * 1

    override fun onStart() {
        mapLocations.respawn.clone().apply {
            x += Random.nextInt(-6, 6)
            z += Random.nextInt(-6, 6)
        }.also {
            participationPlayerList.forEach {
                it.teleport(it)
            }
        }

        gameSchedulers.getScheduler(ScoreOfPushScheduler.Code.SCORE).runTick(1, Int.MAX_VALUE)
        gameSchedulers.getScheduler(ScoreOfPushScheduler.Code.MAIN).runSecond(1, playTime)
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.teleport(mapLocations.respawn)
                playerGameStatusManager.setStatus(player.uniqueId, PlayerStatus.ALIVE)
            }
        }
    }

    override fun gameStopCondition(): Boolean {
        return false
    }

}