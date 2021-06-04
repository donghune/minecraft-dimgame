package com.github.donghune.dimgame.minigame.battle_of_push


import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.minigame.DimGameMap
import com.github.donghune.dimgame.minigame.DimGameOption
import com.github.donghune.dimgame.minigame.MiniGame
import com.github.donghune.namulibrary.extension.sendInfoMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*

class BattleOfPush : MiniGame<BattleOfPushItem, BattleOfPushScheduler>(
    name = ChatColor.DARK_PURPLE.toString() + "밀치기 전투",
    description = "동그란 섬 밖으로 상대를 내보내세요!",
    mapLocations = DimGameMap(
        BoundingBox(543.0, 97.0, 92.0, 503.0, 77.0, 132.0),
        Location(Bukkit.getWorld("world"), 523.0, 87.0, 112.0),
    ),
    gameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = true,
        isChat = true
    )
) {

    override val gameItems: BattleOfPushItem = BattleOfPushItem()
    override val gameSchedulers: BattleOfPushScheduler = BattleOfPushScheduler(this)

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        Bukkit.getOnlinePlayers().forEach { it.inventory.addItem(gameItems.getItemById(BattleOfPushItem.Code.STICK)) }
        gameSchedulers.getScheduler(BattleOfPushScheduler.Code.RANDOM_ITEM).runSecond(8, Int.MAX_VALUE)
    }

    override fun onStop(rank: List<Player>) {
        gameSchedulers.getScheduler(BattleOfPushScheduler.Code.RANDOM_ITEM).stopScheduler()
    }

    @EventHandler
    fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player

        player.gameMode = GameMode.SPECTATOR
        player.teleport(mapLocations.respawn)

        finishedPlayerList.add(player.uniqueId)
        Bukkit.getOnlinePlayers().forEach { it.sendInfoMessage("${player.name}님이 탈락하셨습니다.") }

        if (gameStopCondition()) {
            finishedPlayerList.add(alivePlayers[0].uniqueId)
            stopGame(finishedPlayerList.reversed().map { Bukkit.getPlayer(it)!! }.toList())
        }
    }

    override fun gameStopCondition(): Boolean {
        return alivePlayers.size == 1
    }

}