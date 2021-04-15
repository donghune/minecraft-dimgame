package com.namu.dimgame.minigame.games.ox_quiz

import com.namu.dimgame.minigame.DimGameScheduler
import com.namu.dimgame.util.clearChat
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Sound
import org.bukkit.entity.Firework
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.Comparator

class OXQuizScheduler(dimGame: OXQuiz) : DimGameScheduler<OXQuizScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            lateinit var currentQuiz: Quiz
            started {
                // potion clear
                dimGame.participationPlayerList.forEach {
                    it.activePotionEffects.forEach { potionEffect: PotionEffect ->
                        it.removePotionEffect(potionEffect.type)
                    }
                }
                // set random quiz
                currentQuiz = quizList.random()

                Bukkit.getOnlinePlayers().forEach {
                    it.clearChat()
                    currentQuiz.printQuizContent(it)
                }
            }
            doing { count ->
                // count down
                dimGame.participationPlayerList.forEach {
                    it.sendTitle((10 - count).toString(), "", 0, 20, 0)
                }
            }
            finished {
                Bukkit.getOnlinePlayers().forEach {
                    it.clearChat()
                    currentQuiz.printQuizAnswer(it)
                }
                // potion effect & score calculate
                dimGame.participationPlayerList.forEach {
                    it.addPotionEffect(
                            PotionEffect(PotionEffectType.SLOW, Int.MAX_VALUE, 10000, false, false, false)
                    )
                    it.addPotionEffect(
                            PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 10000, false, false, false)
                    )
                }

                val answerLocation = if (currentQuiz.answer) {
                    dimGame.blueCenterLocation
                } else {
                    dimGame.redCenterLocation
                }

                answerLocation.world.apply {
                    playSound(answerLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                    spawn(answerLocation, Firework::class.java).apply {
                        fireworkMeta = fireworkMeta.apply {
                            addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.LIME).build())
                            power = 0
                        }
                    }
                }

                dimGame.participationPlayerList
                        .filter { it.location.distance(answerLocation) <= 11 }
                        .forEach { dimGame.uuidByScore[it.uniqueId] = (dimGame.uuidByScore[it.uniqueId] ?: 0) + 1 }
            }
        }.registerScheduler(Code.QUIZ)

        SchedulerManager {
            doing {
                if (it >= cycle) {
                    return@doing
                }
                getScheduler(Code.QUIZ).runSecond(1, 10)
            }
            finished {
                dimGame.participationPlayerList.forEach {
                    it.activePotionEffects.forEach { potionEffect: PotionEffect ->
                        it.removePotionEffect(potionEffect.type)
                    }
                }

                dimGame.stopGame(
                        dimGame.uuidByScore.toSortedMap(
                                Comparator { o1: UUID, o2: UUID ->
                                    return@Comparator (dimGame.uuidByScore[o2] ?: 0) - (dimGame.uuidByScore[o1] ?: 0)
                                }
                        ).keys
                                .mapNotNull { Bukkit.getPlayer(it) }
                                .toList()
                )
            }
        }.registerScheduler(Code.MAIN)
    }

    enum class Code {
        MAIN, QUIZ
    }
}