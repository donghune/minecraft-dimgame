package com.namu.dimgame.repository.other

import com.github.namu0240.namulibrary.scoreboard.ScoreBoardManager
import com.namu.dimgame.repository.score.AbstractPlayerScoreRepository
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object DimGameScoreBoard {
    fun updatePlayerScoreBoard(player: Player, scoreRepository: AbstractPlayerScoreRepository) {
        val playerScoreBoard = ScoreBoardManager("Dim-Game")
        val boardContent = mutableListOf<String>()

        boardContent.add("내 정보")
        scoreRepository.getPlayerScore(player.uniqueId).also {
            boardContent.add("총점 : ★ x $it")
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
}