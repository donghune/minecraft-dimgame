package com.github.donghune.dimgame.minigame.ox_quiz

import com.github.donghune.dimgame.minigame.MiniGameScheduler
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.util.contains2D
import com.github.donghune.namulibrary.schedular.SchedulerManager
import com.github.shynixn.mccoroutine.launch
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.Comparator

class OXQuizScheduler(dimGame: OXQuiz) : MiniGameScheduler<OXQuizScheduler.Code>(dimGame) {

    init {
        SchedulerManager {
            lateinit var currentQuiz: Quiz
            started {
                // set random quiz
                currentQuiz = quizList.random()
            }
            doing { count ->
                // count down
                dimGame.participationPlayerList.forEach {
                    it.sendTitle(
                        ChatColor.RED.toString() + (cycle - count % cycle).toString(),
                        ChatColor.GOLD.toString() + currentQuiz.content,
                        0,
                        20,
                        0
                    )
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1f, 1f)
                }
            }
            finished {
                dimGame.participationPlayerList.forEach {
                    it.sendTitle(
                        if (currentQuiz.answer) ChatColor.BLUE.toString() + "O" else ChatColor.RED.toString() + "X",
                        "",
                        0,
                        60,
                        0
                    )
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1f, 1f)
                }

                val answerArea = if (currentQuiz.answer) {
                    dimGame.blueArea
                } else {
                    dimGame.redArea
                }

                val answerCenterLocation = answerArea.center.toLocation(Bukkit.getWorld("world")!!)

                answerCenterLocation.world!!.apply {
                    playSound(answerCenterLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)
                    spawn(answerCenterLocation, Firework::class.java).apply {
                        fireworkMeta = fireworkMeta.apply {
                            addEffect(
                                FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.LIME)
                                    .build()
                            )
                            power = 0
                        }
                    }
                }

                dimGame.participationPlayerList
                    .filter { answerArea.contains2D(it.location.toVector()) }
                    .onEach { player ->
                        player.addPotionEffect(
                            PotionEffect(
                                PotionEffectType.GLOWING,
                                (20L * 2).toInt(),
                                1,
                                false,
                                false,
                                false
                            )
                        )
                    }
                    .forEach { dimGame.uuidByScore[it.uniqueId] = (dimGame.uuidByScore[it.uniqueId] ?: 0) + 1 }

                // potion clear
                currentCycle = 0
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
                dimGame.participationPlayerList
                    .associate { it.uniqueId to 0 }.toMutableMap()
                    .also { dimGame.uuidByScore.forEach { (uuid, score) -> it[uuid] = score } }
                    .toSortedMap(Comparator { o1: UUID, o2: UUID ->
                        return@Comparator (dimGame.uuidByScore[o2] ?: 0) - (dimGame.uuidByScore[o1] ?: 0)
                    }).keys
                    .mapNotNull { Bukkit.getPlayer(it) }
                    .toList()
                    .also {
                        plugin.launch {
                            dimGame.stopGame(it)
                        }
                    }
            }
        }.registerScheduler(Code.MAIN)
    }

    enum class Code {
        MAIN, QUIZ
    }
}