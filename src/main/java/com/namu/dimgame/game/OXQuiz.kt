package com.namu.dimgame.game

import com.namu.dimgame.entity.*
import com.namu.dimgame.entity.quiz.Quiz
import com.namu.dimgame.entity.quiz.quizList
import com.namu.dimgame.plugin
import com.namu.dimgame.util.clearChat
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class OXQuiz : DimGame() {
    override val name: String = "OX Quiz"
    override val description: String = "살아남으세요!"
    override val gameType: GameType = GameType.RANK
    override val mapLocations: GameMap = GameMap(
        Location(Bukkit.getWorld("world"), 348.0, 95.0, 269.0),
        Location(Bukkit.getWorld("world"), 399.0, 79.0, 299.0),
        Location(Bukkit.getWorld("world"), 373.0, 85.0, 284.0),
    )

    private val redCenterLocation = Location(Bukkit.getWorld("world"), 384.0, 85.0, 284.0)
    private val blueCenterLocation = Location(Bukkit.getWorld("world"), 363.0, 85.0, 284.0)

    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false
    )
    override val defaultItems: List<ItemStack> = emptyList()

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.ADVENTURE
        }

        oxQuizGameSchedulerManager.runSecond(12, 5)
    }

    override fun onStop(rank: List<Player>) {
        AsyncPlayerChatEvent.getHandlerList().unregister(this)
        participationPlayerList.forEach {
            it.gameMode = GameMode.SURVIVAL
        }
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerState) {
        when (playerState) {
            PlayerState.ALIVE -> {

            }
            PlayerState.DIE -> {

            }
        }
    }

    private val uuidByScore = mutableMapOf<UUID, Int>()

    private val oxQuizGameSchedulerManager
        get() = SchedulerManager {
            started {

            }
            doing {
                if (it >= cycle) {
                    return@doing
                }
                oxQuizSchedulerManager.runSecond(1, 10)
            }
            finished {
                participationPlayerList.forEach {
                    it.activePotionEffects.forEach { potionEffect: PotionEffect ->
                        it.removePotionEffect(potionEffect.type)
                    }
                }

                stopMiniGame(
                    uuidByScore.toSortedMap(
                        Comparator { o1: UUID, o2: UUID ->
                            return@Comparator (uuidByScore[o2] ?: 0) - (uuidByScore[o1] ?: 0)
                        }
                    ).keys
                        .mapNotNull { Bukkit.getPlayer(it) }
                        .toList()
                )
            }
        }

    private val oxQuizSchedulerManager
        get() = SchedulerManager {
            lateinit var currentQuiz: Quiz
            started {
                // potion clear
                participationPlayerList.forEach {
                    it.activePotionEffects.forEach { potionEffect: PotionEffect ->
                        it.removePotionEffect(potionEffect.type)
                    }
                }
                // set random quiz
                currentQuiz = quizList.random()

                Bukkit.getOnlinePlayers().forEach {
                    it.clearChat()
                    it.sendMessage("========= [ QUIZ ] =========")
                    it.sendMessage("")
                    it.sendMessage("")
                    it.sendMessage("Q. ${currentQuiz.content}")
                    it.sendMessage("")
                    it.sendMessage("")
                    it.sendMessage("========= [ QUIZ ] =========")
                }
            }
            doing { count ->
                // count down
                participationPlayerList.forEach {
                    it.sendTitle((10 - count).toString(), "", 0, 20, 0)
                }
            }
            finished {
                Bukkit.getOnlinePlayers().forEach {
                    it.clearChat()
                    it.sendMessage("========= [ QUIZ ] =========")
                    it.sendMessage("")
                    it.sendMessage("")
                    it.sendMessage("Answer. ${currentQuiz.answer}")
                    it.sendMessage("")
                    it.sendMessage("")
                    it.sendMessage("========= [ QUIZ ] =========")
                }
                // potion effect & score calculate
                participationPlayerList.forEach {
                    it.addPotionEffect(
                        PotionEffect(PotionEffectType.SLOW, Int.MAX_VALUE, 10000, false, false, false)
                    )
                    it.addPotionEffect(
                        PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 10000, false, false, false)
                    )
                }
                val answerLocation = if (currentQuiz.answer) {
                    blueCenterLocation
                } else {
                    redCenterLocation
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

                participationPlayerList
                    .filter { it.location.distance(answerLocation) <= 11 }
                    .forEach { uuidByScore[it.uniqueId] = (uuidByScore[it.uniqueId] ?: 0) + 1 }
            }
        }

    @EventHandler
    fun onPlayerChatEvent(event: AsyncPlayerChatEvent) {
        if (event.player.isOp) {
            return
        }

        event.isCancelled = true
    }

}