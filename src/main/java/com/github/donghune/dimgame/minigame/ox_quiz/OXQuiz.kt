package com.github.donghune.dimgame.minigame.ox_quiz

import com.github.donghune.dimgame.minigame.MiniGameMap
import com.github.donghune.dimgame.minigame.MiniGameOption
import com.github.donghune.dimgame.minigame.MiniGame
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import java.util.*

class OXQuiz : MiniGame<OXQuizItem, OXQuizScheduler>(
    name = ChatColor.YELLOW.toString() + "OX 퀴즈",
    description = "남들보다 많은 문제를 맞추세요!",
    mapLocations = MiniGameMap(
        BoundingBox(348.0, 95.0, 269.0, 399.0, 79.0, 299.0),
        Location(Bukkit.getWorld("world"), 373.0, 85.0, 284.0),
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false,
        isChat = true
    )
) {

    internal val redArea = BoundingBox(
        374.0, 85.0, 294.0,
        394.0, 85.0, 274.0
    )
    internal val blueArea = BoundingBox(
        353.0, 85.0, 274.0,
        373.0, 85.0, 294.0
    )

    override val gameItems: OXQuizItem = OXQuizItem()
    override val gameSchedulers: OXQuizScheduler = OXQuizScheduler(this)

    internal val uuidByScore = mutableMapOf<UUID, Int>()

    override suspend fun onStart() {
        delay(3000L)
        gameSchedulers.getScheduler(OXQuizScheduler.Code.MAIN).runSecond(15, 5)
    }

    override suspend fun onStop(rank: List<Player>) {
    }

    override suspend fun gameStopCondition(): Boolean {
        return false
    }

}