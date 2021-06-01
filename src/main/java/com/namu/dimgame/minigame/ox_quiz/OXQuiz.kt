package com.namu.dimgame.minigame.ox_quiz


import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*

class OXQuiz : DimGame<OXQuizItem, OXQuizScheduler>() {
    override val name: String = ChatColor.YELLOW.toString() + "OX 퀴즈"
    override val description: String = "살아남으세요!"

    override val mapLocations: DimGameMap = DimGameMap(
        Location(Bukkit.getWorld("world"), 348.0, 95.0, 269.0),
        Location(Bukkit.getWorld("world"), 399.0, 79.0, 299.0),
        Location(Bukkit.getWorld("world"), 373.0, 85.0, 284.0),
    )

    internal val redArea = BoundingBox(
        374.0, 85.0, 294.0,
        394.0, 85.0, 274.0
    )

    internal val blueArea = BoundingBox(
        353.0, 85.0, 274.0,
        373.0, 85.0, 294.0
    )

    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false
    )

    override val defaultItems: List<ItemStack> = emptyList()
    override val gameItems: OXQuizItem = OXQuizItem()
    override val gameSchedulers: OXQuizScheduler = OXQuizScheduler(this)

    override fun onStart() {
        participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.ADVENTURE
        }
        gameSchedulers.getScheduler(OXQuizScheduler.Code.MAIN).runSecond(15, 5)
    }

    override fun onStop(rank: List<Player>) {
        AsyncPlayerChatEvent.getHandlerList().unregister(this)
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {

            }
        }
    }

    internal val uuidByScore = mutableMapOf<UUID, Int>()

    @EventHandler
    fun onPlayerChatEvent(event: AsyncPlayerChatEvent) {
        if (event.player.isOp) {
            return
        }

        event.isCancelled = true
    }

    override fun gameStopCondition(): Boolean {
        return false
    }

}