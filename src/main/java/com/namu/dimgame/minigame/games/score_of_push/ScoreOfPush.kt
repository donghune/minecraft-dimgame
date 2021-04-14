package com.namu.dimgame.minigame.games.score_of_push


import com.namu.dimgame.minigame.*
import com.namu.dimgame.plugin
import com.namu.namulibrary.extension.ItemBuilder
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random

class ScoreOfPush : DimGame() {
    override val name: String = "점수 얻기 ( 밀치기 )"
    override val description: String = "빨간색 원 위에서 점수를 최대한 많이 얻으세요"
    override val gameType: GameType = GameType.TIME
    override val mapLocations: DimGameMap = DimGameMap(
        Location(Bukkit.getWorld("world"), 273.0, 99.0, 20.0),
        Location(Bukkit.getWorld("world"), 346.0, 60.0, -63.0),
        Location(Bukkit.getWorld("world"), 311.5, 86.5, -21.5),
    )
    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = true
    )
    override val defaultItems: List<ItemStack> = listOf(
        ItemBuilder().setMaterial(Material.STICK)
            .setDisplay("막대기")
            .setLore(listOf("이걸로 상대방을 밀어버리자"))
            .build().apply {
                addUnsafeEnchantment(Enchantment.KNOCKBACK, 3)
            }
    )

    private val uuidByScore = mutableMapOf<UUID, Int>()
    private val bossBar = Bukkit.createBossBar("남은시간 %02d:%02d", BarColor.BLUE, BarStyle.SEGMENTED_20)
    private val playTime = 60 * 2;

    private val gameTimeScheduler = SchedulerManager {
        started {
            Bukkit.getOnlinePlayers()
                .forEach {
                    bossBar.addPlayer(it)
                }
        }
        doing {
            bossBar.setTitle("남은시간 %02d:%02d".format((playTime - it) / 60, (playTime - it) % 60))
            bossBar.progress = 1.0 - (it / playTime).toDouble()
        }
        finished {
            bossBar.players.forEach { bossBar.removePlayer(it) }
            stopGame(
                uuidByScore.toList()
                    .sortedByDescending { it.second }
                    .mapNotNull { Bukkit.getPlayer(it.first) }
                    .toList()
            )
        }
    }

    private val stayScoreScheduler = SchedulerManager {
        doing {
            Bukkit.getOnlinePlayers()
                .filter { mapLocations.respawn.distance(it.location) <= 3.5 }
                .forEach { player ->
                    val uuid = player.uniqueId
                    uuidByScore[uuid] = (uuidByScore[uuid] ?: 0) + 1
                    player.level = uuidByScore[uuid] ?: 0
                    if (it % 4 == 0) {
                        player.playSound(player.location, Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 0.05f, 0.7f)
                    }
                }
        }
    }

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        participationPlayerList.forEach {
            it.teleport(
                mapLocations.respawn.clone().apply {
                    x += Random.nextInt(-6, 6)
                    z += Random.nextInt(-6, 6)
                }
            )
        }

        stayScoreScheduler.runTick(1, Int.MAX_VALUE)
        gameTimeScheduler.runSecond(1, playTime)
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        stayScoreScheduler.stopScheduler()
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.teleport(mapLocations.respawn)
                playerGameStatusManager.setStatus(player.uniqueId, PlayerStatus.ALIVE)
            }
        }
    }

}