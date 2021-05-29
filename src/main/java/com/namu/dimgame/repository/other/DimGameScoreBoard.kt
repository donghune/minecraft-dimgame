package com.namu.dimgame.repository.other

import com.namu.dimgame.plugin
import com.namu.dimgame.repository.score.AbstractPlayerScoreRepository
import com.namu.dimgame.util.ScoreBoardManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object DimGameScoreBoard {
    fun updatePlayerScoreBoard(player: Player, scoreRepository: AbstractPlayerScoreRepository) {
        val playerScoreBoard = ScoreBoardManager("Dim-Game")
        val boardContent = mutableListOf<String>()

        boardContent.add("내 정보")
        scoreRepository.getPlayerScore(player.uniqueId).also {
            boardContent.add("총점 : " + ChatColor.GREEN.toString() + "★" + ChatColor.WHITE.toString() + " x $it")
            boardContent.add("")
        }

        // Overall Ranking
        boardContent.add("전체 순위")
        scoreRepository.getRank()
            .map { it.key to it.value }
            .subList(0, 3)
            .forEachIndexed { index, pair ->
                boardContent.add("${index + 1}등 ${Bukkit.getPlayer(pair.first)?.displayName}")
            }

        playerScoreBoard.setBoardContent(boardContent)
        playerScoreBoard.visibleScoreboard(player)
    }

    fun clearPlayerScoreBoard() {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                player.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
            }
        }, 60L)
    }

}