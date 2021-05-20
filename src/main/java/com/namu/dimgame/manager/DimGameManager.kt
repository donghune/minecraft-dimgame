package com.namu.dimgame.manager

import com.namu.dimgame.minigame.*
import com.namu.dimgame.repository.other.DimGameScoreBoard
import com.namu.dimgame.repository.other.ParticleResources
import com.namu.dimgame.repository.participant.AbstractParticipantRepository
import com.namu.dimgame.repository.participant.ParticipantStatusRepository
import com.namu.dimgame.repository.score.AbstractPlayerScoreRepository
import com.namu.dimgame.repository.score.PlayerScoreRepository
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.Location

class DimGameManager : AbstractDimGameManager() {

    override val lobbyLocation: Location = Location(Bukkit.getWorld("world"), 241.0, 86.0, 263.0)

    override val scoreRepository: AbstractPlayerScoreRepository = PlayerScoreRepository()
    override val participantRepository: AbstractParticipantRepository = ParticipantStatusRepository()

    override var gameState: GameStatus = GameStatus.NOT_PLAYING
    override var roundGameState: RoundGameStatus = RoundGameStatus.WAITING
    override var round: Int = 0

    override fun start() {
        Bukkit.setWhitelist(true)
        round = 0
        gameState = GameStatus.PLAYING
        dimGameList = getNewGameList().shuffled()
        roundGameState = RoundGameStatus.WAITING
        scoreRepository.clearAllPlayerScore()
        Bukkit.getOnlinePlayers().forEach { player -> player.teleport(lobbyLocation) }
        startRound()
    }

    override fun stop() {
        gameState = GameStatus.NOT_PLAYING
        Bukkit.setWhitelist(false)
        scoreRepository.showMVPPlayer()
        scoreRepository.clearAllPlayerScore()
        ParticleResources.executeMVPParticle(lobbyLocation)
    }

    override fun skip() {
        currentDimGame.skipGame()
        startRound()
    }

    override fun startRound() {
        if (round == dimGameList.size) {
            stop()
            return
        }

        currentDimGame = dimGameList[round]

        SchedulerManager {
            doing {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle(currentDimGame.name, (cycle - currentCycle).toString(), 0, 20, 0)
                }
            }
            finished {
                roundGameState = RoundGameStatus.RUNNING
                currentDimGame.startGame(
                    participantRepository.getParticipantList(),
                    participantRepository.getObserverList()
                ) { rankPlayerList ->
                    roundGameState = RoundGameStatus.WAITING
                    rankPlayerList.forEachIndexed { index, player ->
                        if (index >= 3) {
                            return@forEachIndexed
                        }
                        scoreRepository.modifyPlayerScore(player.uniqueId, 3 - index)
                    }

                    Bukkit.getOnlinePlayers().forEach {
                        it.teleport(lobbyLocation)
                        it.level = 0
                        it.exp = 0f
                        DimGameScoreBoard.updatePlayerScoreBoard(it, scoreRepository)
                    }
                    round++
                    startRound()
                }
            }
        }.runSecond(1, 10)
    }

}