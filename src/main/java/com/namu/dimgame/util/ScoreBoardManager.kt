package com.namu.dimgame.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class ScoreBoardManager(
    var objectiveName: String,
    var boardDisplaySlot: DisplaySlot = DisplaySlot.SIDEBAR
) {

    private var scoreboard: Scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard

    fun setBoardContent(values: List<String>) {
        scoreboard.getObjective(objectiveName)?.unregister()
        scoreboard.registerNewObjective(
            objectiveName,
            "Dummy",
            objectiveName
        ).apply {
            displaySlot = boardDisplaySlot
            values.mapIndexed { index, value ->
                (if (value == "") getEmptyLine(index) else value) to values.size - index
            }.forEach { value ->
                getScore(value.first).apply {
                    score = value.second
                }
            }
        }
    }

    fun visibleScoreboard(player: Player) {
        player.scoreboard = scoreboard
    }

    fun invisibleScoreboard(player: Player) {
        player.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
    }

    private fun getEmptyLine(index: Int): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until index) {
            stringBuilder.append("§f")
        }
        return stringBuilder.toString()
    }
}