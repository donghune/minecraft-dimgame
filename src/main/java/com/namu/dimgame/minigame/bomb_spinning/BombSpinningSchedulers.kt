package com.namu.dimgame.minigame.bomb_spinning

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.dimgame.manager.PlayerStatus
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class BombSpinningSchedulers(
    dimGame: BombSpinning
) : DimGameScheduler<BombSpinningSchedulers.Code>(dimGame) {

    private val bombPotionEffect = PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, true, false, true)

    init {
        SchedulerManager {
            doing { currentCycle ->
                // 5초 이상 일 경우 스킵
                if (cycle - currentCycle > 5) {
                    return@doing
                }

                // 현재 봄버맨을 가져옴
                dimGame.getBombMan()?.let {
                    // 봄버맨 각성
                    it.addPotionEffect(bombPotionEffect)

                    // 봄버맨 각성 파티클 소환
                    getScheduler(Code.BOMB_MAC_PARTICLE).runTick(2, 10)
                }
            }
            finished {
                // 현재 봄버맨을 가져옴
                dimGame.getBombMan()?.let { bombMan ->
                    // 봄버맨 폭팔
                    bombMan.world.createExplosion(bombMan.location, 1f, false, false)

                    // 봄버맨 주위의 엔티티 한마리 사망
                    val target = bombMan.getNearbyEntities(1.0, 1.0, 1.0)
                        .filterIsInstance<Player>()
                        .firstOrNull { bombMan.gameMode != GameMode.SPECTATOR }

                    // 봄버맨 사망
                    dimGame.playerGameStatusManager.setStatus(bombMan.uniqueId, PlayerStatus.DIE)
                    target?.let { nearPlayer ->
                        dimGame.playerGameStatusManager.setStatus(
                            nearPlayer.uniqueId,
                            PlayerStatus.DIE
                        )
                    }
                }

                // 살아남은 플레이어가 2명 이상 일 경우 게임 지속
                if (dimGame.alivePlayers.count() >= 2) {
                    getScheduler(Code.SET_BOMB_MAN).runSecond(1, 2)
                }
            }
        }.registerScheduler(Code.MAIN)

        SchedulerManager {
            finished {
                // 랜덤으로 봄버맨을 선택
                dimGame.setRandomBombMan()
                // 15초에서 20초 뒤 매인 스케쥴 실행
                getScheduler(Code.MAIN).runSecond(1, Random.nextInt(15, 20))
            }
        }.registerScheduler(Code.SET_BOMB_MAN)

        SchedulerManager {
            doing { _ ->
                dimGame.getBombMan()?.let {
                    it.world.spawnParticle(Particle.LAVA, it.location.add(0.0, 2.0, 0.0), 5)
                }
            }
        }.registerScheduler(Code.BOMB_MAC_PARTICLE)
    }

    enum class Code {
        MAIN, SET_BOMB_MAN, BOMB_MAC_PARTICLE
    }
}