package com.github.donghune.dimgame.repository.score

import org.bukkit.entity.Player
import java.util.*

abstract class AbstractPlayerScoreRepository {
    abstract fun getPlayerScore(uuid: UUID): Int

    abstract fun setPlayerScore(uuid: UUID, value: Int)

    abstract fun modifyPlayerScore(uuid: UUID, value: Int)

    abstract fun getRank() : SortedMap<UUID, Int>

    abstract fun clearAllPlayerScore()

    abstract fun getMVPPlayer(): Player

    abstract fun showMVPPlayer()
}