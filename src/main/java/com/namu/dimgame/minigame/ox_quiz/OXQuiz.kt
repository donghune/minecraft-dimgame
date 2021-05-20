package com.namu.dimgame.minigame.ox_quiz


import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class OXQuiz : DimGame<OXQuizItem, OXQuizScheduler>() {
    override val name: String = "OX Quiz"
    override val description: String = "살아남으세요!"

    override val mapLocations: DimGameMap = DimGameMap(
            Location(Bukkit.getWorld("world"), 348.0, 95.0, 269.0),
            Location(Bukkit.getWorld("world"), 399.0, 79.0, 299.0),
            Location(Bukkit.getWorld("world"), 373.0, 85.0, 284.0),
    )

    internal val redCenterLocation = Location(Bukkit.getWorld("world"), 384.0, 85.0, 284.0)
    internal val blueCenterLocation = Location(Bukkit.getWorld("world"), 363.0, 85.0, 284.0)

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
        participationPlayerList.forEach {
            it.gameMode = GameMode.SURVIVAL
        }
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