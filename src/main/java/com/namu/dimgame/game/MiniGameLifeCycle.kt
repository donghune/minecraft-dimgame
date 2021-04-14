package com.namu.dimgame.game

import com.namu.dimgame.minigame.DimGame
import com.namu.dimgame.minigame.GameStatus
import org.bukkit.Bukkit
import java.util.*

abstract class MiniGameLifeCycle {

    private val playerStateManager = PlayerParticipantStatusManager()
    private val loadGameList: List<DimGame<*, *>> = listOf()
    private var selectedGameList: MutableList<DimGame<*, *>> = mutableListOf()
    private val maxRound = 1

    protected var gameState = GameStatus.NOT_PLAYING

    private lateinit var dimGame: DimGame<*, *>

    /**
     * 게임 시작
     */
    fun start(): Boolean {
        if (gameState == GameStatus.PLAYING) {
            return false
        }

        // 게임 상태 변경
        gameState = GameStatus.PLAYING

        // 선택된 게임을 초기화 후 랜덤으로 게임을 배치
        selectedGameList.clear()
        selectedGameList = loadGameList.shuffled().subList(0, maxRound).toMutableList()

        onStart()
        startNextGame(0)
        return true
    }

    /**
     * 게임 강제 종료
     */
    fun forcedTermination(): Boolean {
        if (gameState == GameStatus.NOT_PLAYING) {
            return false
        }

        // 게임 상태 변경
        gameState = GameStatus.NOT_PLAYING

        // 현재 게임 종료
        dimGame.stopGame(listOf())

        onStop()
        return true
    }

    /**
     * 다음 게임 시작
     */
    private fun startNextGame(round: Int) {

        // 마지막 라운드 게임 일 경우 종료
        if (round == maxRound) {

            // 게임 상태 변경
            gameState = GameStatus.NOT_PLAYING

            onStop()
            return
        }

        onRoundStart()
        selectedGameList[round].startGame(
            Bukkit.getOnlinePlayers()
                .filter { player -> playerStateManager.getStatus(player.uniqueId) == ParticipantStatus.PARTICIPANT }
                .toList(),
            Bukkit.getOnlinePlayers()
                .filter { player -> playerStateManager.getStatus(player.uniqueId) == ParticipantStatus.OBSERVER }
                .toList()
        ) { rankPlayerList ->
            onRoundEnd(rankPlayerList.map { player -> player.uniqueId })
            startNextGame(round + 1)
        }
    }

    // lifecycles

    protected abstract fun onStart()

    protected abstract fun onRoundStart()

    protected abstract fun onRoundEnd(rank: List<UUID>)

    protected abstract fun onStop()

}

