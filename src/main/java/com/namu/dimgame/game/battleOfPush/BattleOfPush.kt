package com.namu.dimgame.game.battleOfPush

import com.namu.dimgame.entity.*
import com.namu.dimgame.plugin
import com.namu.namulibrary.extension.ItemBuilder
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.random.Random

class BattleOfPush : DimGame() {
    override val name: String = "밀치기 전투"
    override val description: String = "동그란 섬 밖으로 상대를 내보내세요!"
    override val gameType: GameType = GameType.RANK
    override val mapLocations: GameMap = GameMap(
        Location(Bukkit.getWorld("world"), 543.0, 97.0, 92.0),
        Location(Bukkit.getWorld("world"), 503.0, 77.0, 132.0),
        Location(Bukkit.getWorld("world"), 523.0, 87.0, 112.0),
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

    companion object {
        private val GAME_ITEM_FLY = ItemBuilder().setMaterial(Material.FEATHER)
            .setDisplay("날개")
            .setLore(listOf("공중부양을 5초간 부여합니다."))
            .build()


        private val GAME_ITEM_SPEED = ItemBuilder().setMaterial(Material.RABBIT_HIDE)
            .setDisplay("신속")
            .setLore(listOf("3초간 신속1이 부여됩니다."))
            .build()

    }

    private var randomItemScheduler: SchedulerManager = SchedulerManager {
        doing {
            participationPlayerList.forEach {
                it.inventory.addItem(gameItems.random())
            }
        }
    }

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        participationPlayerList.forEach {
            it.teleport(
                mapLocations.respawn.apply {
                    x += Random.nextInt(-6, 6)
                    z += Random.nextInt(-6, 6)
                }
            )
        }

        randomItemScheduler.runSecond(8, Int.MAX_VALUE)

        registerGameItem(GAME_ITEM_FLY) {
            val potionEffect = PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 1, true, false)
            it.player.addPotionEffect(potionEffect)
        }
        registerGameItem(GAME_ITEM_SPEED) {
            val potionEffect = PotionEffect(PotionEffectType.SPEED, 20 * 3, 0, true, false)
            it.player.addPotionEffect(potionEffect)
        }
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        randomItemScheduler.stopScheduler()
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerState) {
        when (playerState) {
            PlayerState.ALIVE -> {

            }
            PlayerState.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)

                val alivePlayers = participationPlayerList.filter { getPlayerState(it.uniqueId) == PlayerState.ALIVE }

                if (alivePlayers.size > 2) {
                    return
                }

                finishedPlayerList.add(player.uniqueId)

                if (alivePlayers.size == 1) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopMiniGame(finishedPlayerList.apply { reverse() }.map { Bukkit.getPlayer(it)!! }.toList())
                }
            }
        }
    }

}