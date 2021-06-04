package com.github.donghune.dimgame.minigame.spleef


import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.events.PlayerStatusChangeEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.repository.ingame.miniGameStatus


import com.github.donghune.namulibrary.extension.sendInfoMessage
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*
import kotlin.random.Random

class Spleef : MiniGame<SpleefItem, SpleefSchedulers>(
    name = ChatColor.RED.toString() + "스플리프",
    description = "상대방을 떨어뜨리고 살아남으세요!",
    mapLocations = DimGameMap(
        BoundingBox(481.0, 100.0, 370.0, 503.0, 30.0, 348.0),
        Location(Bukkit.getWorld("world"), 492.0, 94.0, 359.0)
    ),
    gameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = true,
        isCraft = false,
        isAttack = false,
        isChat = true,
    )
) {

    override val gameItems: SpleefItem = SpleefItem()
    override val gameSchedulers: SpleefSchedulers = SpleefSchedulers(this)

    override fun onStart() {
        participationPlayerList.forEach { it.inventory.addItem(gameItems.getItemById(SpleefItem.Code.SHOVEL)) }
    }

    override fun onStop(rank: List<Player>) {
        BlockBreakEvent.getHandlerList().unregister(this)

        // 파괴된 블럭 원복
        breakBlockList.forEach {
            Bukkit.getWorld("world")!!.getBlockAt(it.location).type = Material.SNOW_BLOCK
        }

        if (rank.isNotEmpty()) {
            Bukkit.getOnlinePlayers().forEach {
                it.sendTitle("", "${rank[0].name} is Winner", 10, 20, 10)
            }
        }
    }

    private val finishedPlayerList = mutableListOf<UUID>()

    @EventHandler
    fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player

        player.gameMode = GameMode.SPECTATOR
        player.teleport(mapLocations.respawn)

        Bukkit.broadcast(Component.text("${player.name}님이 탈락하셨습니다."))
        finishedPlayerList.add(player.uniqueId)

        if (gameStopCondition()) {
            finishedPlayerList.add(alivePlayers[0].uniqueId)
            stopGame(finishedPlayerList.map { Bukkit.getPlayer(it)!! }.reversed())
        }
    }

    override fun gameStopCondition(): Boolean {
        return alivePlayers.size == 1
    }

    private val breakBlockList = mutableListOf<Block>()

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {

        event.isCancelled = true
        event.block.type = Material.AIR

        breakBlockList.add(event.block)

        val chance = Random.nextInt(1, 100)

        if (chance > 5) {
            return
        }

        event.player.inventory.addItem(gameItems.getActionItemList().random())
    }
}
