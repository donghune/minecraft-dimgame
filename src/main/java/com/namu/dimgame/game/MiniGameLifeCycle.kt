package com.namu.dimgame.game

import com.namu.dimgame.minigame.DimGame
import com.namu.dimgame.minigame.GameStatus
import com.namu.dimgame.minigame.games.battle_of_push.BattleOfPush
import com.namu.dimgame.minigame.games.bomb_spinning.BombSpinning
import com.namu.dimgame.minigame.games.fast_combination.FastCombination
import com.namu.dimgame.minigame.games.jump_map.JumpMap
import com.namu.dimgame.minigame.games.ox_quiz.OXQuiz
import com.namu.dimgame.minigame.games.score_of_push.ScoreOfPush
import com.namu.dimgame.minigame.games.spleef.Spleef
import com.namu.dimgame.minigame.games.to_avoid_anvil.ToAvoidAnvil
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import java.util.*

abstract class MiniGameLifeCycle {

    private val playerStateManager = PlayerParticipantStatusManager()
    private val loadGameList: List<DimGame<*, *>>
        get() = listOf(
            BattleOfPush(),
            BombSpinning(),
            FastCombination(),
            JumpMap(),
            OXQuiz(),
            ScoreOfPush(),
            Spleef(),
            ToAvoidAnvil()
        )
    private var selectedGameList: MutableList<DimGame<*, *>> = mutableListOf()
    private val maxRound = loadGameList.size

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

    private val nextGameScheduler: (DimGame<*, *>, () -> Unit) -> SchedulerManager = { dimGame, countDownEndCallback ->
        SchedulerManager {
            doing {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle(dimGame.name, (cycle - currentCycle).toString(), 0, 20, 0)
                }
            }
            finished {
                countDownEndCallback.invoke()
            }
        }
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
        gameState = GameStatus.NOT_PLAYING

        onRoundStart()

        nextGameScheduler(selectedGameList[round]) {
            gameState = GameStatus.PLAYING
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
        }.runSecond(1L, 5)
    }

    // lifecycles

    protected abstract fun onStart()

    protected abstract fun onRoundStart()

    protected abstract fun onRoundEnd(rank: List<UUID>)

    protected abstract fun onStop()

}

