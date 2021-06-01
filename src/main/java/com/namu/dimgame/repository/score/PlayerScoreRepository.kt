package com.namu.dimgame.repository.score

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class PlayerScoreRepository : AbstractPlayerScoreRepository() {

    private val uuidByScore: MutableMap<UUID, Int> = mutableMapOf()

    override fun getPlayerScore(uuid: UUID): Int {
        if (uuidByScore[uuid] == null) {
            setPlayerScore(uuid, 0)
        }
        return uuidByScore[uuid]!!
    }

    override fun getRank(): SortedMap<UUID, Int> {
        println(uuidByScore)
        return uuidByScore.toSortedMap { player1, player2 ->
            return@toSortedMap if (getPlayerScore(player1) == getPlayerScore(player2)) {
                -1
            } else {
                getPlayerScore(player2) - getPlayerScore(player1)
            }
        }
    }

    override fun setPlayerScore(uuid: UUID, value: Int) {
        uuidByScore[uuid] = value
    }

    override fun modifyPlayerScore(uuid: UUID, value: Int) {
        uuidByScore[uuid] = getPlayerScore(uuid) + value
    }

    override fun clearAllPlayerScore() {
        uuidByScore.keys.forEach { uuid -> uuidByScore[uuid] = 0 }
    }

    override fun getMVPPlayer(): Player {
        val maxScore = uuidByScore.values.maxOrNull() ?: 0
        return Bukkit.getPlayer(uuidByScore.filter { it.value == maxScore }.keys.first())!!
    }

    override fun showMVPPlayer() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle("MVP", getMVPPlayer().displayName, 10, 60, 10)
        }
    }

}