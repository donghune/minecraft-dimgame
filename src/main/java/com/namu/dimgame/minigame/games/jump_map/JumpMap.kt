package com.namu.dimgame.minigame.games.jump_map


import com.namu.dimgame.minigame.*
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

class JumpMap : DimGame<JumpMapItem, JumpMapScheduler>() {

    private val goalBlockLocation = Location(Bukkit.getWorld("world"), 267.0, 97.0, 110.0)

    override val name: String = "점프맵"
    override val description: String = "누구보다 빨리 맵의 끝에 있는 에메랄드 블럭을 터치하세요!"

    override val mapLocations: DimGameMap = DimGameMap(
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
    override val gameItems: JumpMapItem = JumpMapItem()
    override val gameSchedulers: JumpMapScheduler = JumpMapScheduler(this)
    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        gameSchedulers.getScheduler(JumpMapScheduler.Code.RANDOM_ITEM).runSecond(10, Int.MAX_VALUE)
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        gameSchedulers.getScheduler(JumpMapScheduler.Code.RANDOM_ITEM).stopScheduler()
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

        if (!gameStopCondition()) {
            return
        }

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            stopGame(finishedPlayerList.map { Bukkit.getPlayer(it)!! }.toList())
        }, 20L * 2)

    }

    override fun gameStopCondition(): Boolean {
        if (participationPlayerList.size < 3) {
            return finishedPlayerList.size == participationPlayerList.size
        }
        return finishedPlayerList.size == 3
    }

}