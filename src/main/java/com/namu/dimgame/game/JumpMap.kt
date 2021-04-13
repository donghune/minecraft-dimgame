package com.namu.dimgame.game

import com.namu.dimgame.entity.*
import com.namu.dimgame.plugin
import com.namu.namulibrary.extension.ItemBuilder
import com.namu.namulibrary.extension.replaceChatColorCode
import com.namu.namulibrary.extension.sendInfoMessage
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class JumpMap : DimGame() {

    private val goalBlockLocation = Location(Bukkit.getWorld("world"), 267.0, 97.0, 110.0)

    override val name: String = "점프맵"
    override val description: String = "누구보다 빨리 맵의 끝에 있는 에메랄드 블럭을 터치하세요!"
    override val gameType: GameType = GameType.RANK
    override val mapLocations: GameMap = GameMap(
        Location(Bukkit.getWorld("world"), 256.0, 112.0, 213.0),
        Location(Bukkit.getWorld("world"), 279.0, 89.0, 105.0),
        Location(Bukkit.getWorld("world"), 267.0, 96.0, 208.0)
    )

    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false,
    )

    override val defaultItems: List<ItemStack> = listOf()

    companion object {
        private val GAME_ITEM_FLY = ItemBuilder().setMaterial(Material.FEATHER)
            .setDisplay("날개")
            .setLore(listOf("공중부양을 5초간 부여합니다."))
            .build()


        private val GAME_ITEM_JUMP = ItemBuilder().setMaterial(Material.RABBIT_FOOT)
            .setDisplay("점프")
            .setLore(listOf("점프강화 2를 3초간 부여합니다."))
            .build()

    }

    private val randomItemScheduler = SchedulerManager {
        doing {
            participationPlayerList.forEach {
                it.inventory.addItem(gameItems.random())
            }
        }
    }

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        randomItemScheduler.also {
            it.runSecond(10, Int.MAX_VALUE)
        }

        registerGameItem(GAME_ITEM_FLY) {
            val potionEffect = PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 1, true, false)
            it.player.addPotionEffect(potionEffect)
        }
        registerGameItem(GAME_ITEM_JUMP) {
            val potionEffect = PotionEffect(PotionEffectType.JUMP, 20 * 3, 2, true, false)
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
                player.teleport(mapLocations.respawn)
                setPlayerState(player, PlayerState.ALIVE)
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.hasBlock().not()) {
            return
        }

        val block = event.clickedBlock ?: return

        if (block.location != goalBlockLocation) {
            return
        }

        if (finishedPlayerList.contains(event.player.uniqueId)) {
            return
        }

        finishedPlayerList.add(event.player.uniqueId)

        block.location.world.spawn(block.location, Firework::class.java).apply {
            fireworkMeta = fireworkMeta.apply {
                addEffect(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.LIME).build())
                power = 0
            }
        }

        block.location.world.playSound(
            block.location,
            Sound.ENTITY_FIREWORK_ROCKET_BLAST,
            1f,
            1f
        )

        Bukkit.getOnlinePlayers().forEach {
            it.sendInfoMessage("&6${event.player.displayName}&f님이 [&6${finishedPlayerList.size}&f]등으로 도착!".replaceChatColorCode())
        }

        if (finishedPlayerList.size == 3) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                stopMiniGame(finishedPlayerList.map { Bukkit.getPlayer(it)!! }.toList())
            }, 20L * 2)
        }

    }

}