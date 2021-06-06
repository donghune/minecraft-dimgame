package com.github.donghune.dimgame.minigame.jump_map


import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.plugin
import com.github.donghune.dimgame.repository.ingame.miniGameStatus
import com.github.donghune.dimgame.util.info
import com.github.donghune.dimgame.util.syncTeleport
import com.github.donghune.namulibrary.extension.replaceChatColorCode
import com.github.donghune.namulibrary.extension.sendInfoMessage
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox
import java.util.*

class JumpMap : MiniGame<JumpMapItem, JumpMapScheduler>(
    name = ChatColor.DARK_RED.toString() + "점프맵",
    description = "누구보다 빨리 맵의 끝에 있는 에메랄드 블럭을 터치하세요!",
    mapLocations = MiniGameMap(
        BoundingBox(256.0, 112.0, 213.0, 279.0, 89.0, 105.0),
        Location(Bukkit.getWorld("world"), 267.0, 96.0, 208.0)
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = false,
        isCraft = false,
        isAttack = false,
        isChat = true,
    )
) {

    override val gameItems: JumpMapItem = JumpMapItem()
    override val gameSchedulers: JumpMapScheduler = JumpMapScheduler(this)

    private val goalBlockLocation = Location(Bukkit.getWorld("world"), 267.0, 97.0, 110.0)
    private val finishedPlayerList = mutableListOf<UUID>()

    override suspend fun onStart() {
        gameSchedulers.getScheduler(JumpMapScheduler.Code.RANDOM_ITEM).runSecond(10, Int.MAX_VALUE)
    }

    override suspend fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        gameSchedulers.getScheduler(JumpMapScheduler.Code.RANDOM_ITEM).stopScheduler()
    }

    @EventHandler
    fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player

        player.syncTeleport(mapLocations.respawn)
        player.miniGameStatus = PlayerMiniGameStatus.ALIVE
    }

    @EventHandler
    suspend fun onPlayerInteractEvent(event: PlayerInteractEvent) {
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

        block.location.world!!.spawn(block.location, Firework::class.java).apply {
            fireworkMeta = fireworkMeta.apply {
                addEffect(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.LIME).build())
                power = 0
            }
        }

        block.location.world!!.playSound(
            block.location,
            Sound.ENTITY_FIREWORK_ROCKET_BLAST,
            1f,
            1f
        )

        Bukkit.broadcast(Component.text(info("&6${event.player.displayName}&f님이 [&6${finishedPlayerList.size}&f]등으로 도착!")))

        if (!gameStopCondition()) {
            return
        }

        delay(2000L)

        stopGame(finishedPlayerList.map { Bukkit.getPlayer(it)!! }.toList())

    }

    override suspend fun gameStopCondition(): Boolean {
        if (participationPlayerList.size < 3) {
            return finishedPlayerList.size == participationPlayerList.size
        }
        return finishedPlayerList.size == 3
    }

}