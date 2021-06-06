package com.github.donghune.dimgame.minigame.fast_combination

import com.github.donghune.dimgame.events.PlayerMiniGameDieEvent
import com.github.donghune.dimgame.manager.PlayerMiniGameStatus
import com.github.donghune.dimgame.minigame.*
import com.github.donghune.dimgame.repository.ingame.miniGameStatus


import com.github.donghune.dimgame.util.ScoreBoardManager
import com.github.donghune.dimgame.util.syncTeleport
import com.github.donghune.namulibrary.schedular.SchedulerManager
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.BoundingBox
import java.util.*
import kotlin.Comparator

class FastCombination : MiniGame<FastCombinationItem, FastCombinationScheduler>(
    name = ChatColor.GOLD.toString() + "빨리 조합하기",
    description = "남들보다 더 많은 아이템을 제작하세요!",
    mapLocations = MiniGameMap(
        BoundingBox(336.0, 118.0, 44.0, 421.0, 80.0, 130.0),
        Location(Bukkit.getWorld("world"), 380.0, 99.0, 96.0),
    ),
    gameOption = MiniGameOption(
        isBlockPlace = false,
        isBlockBreak = true,
        isCraft = true,
        isAttack = false,
        isChat = true
    )
) {

    override val gameItems: FastCombinationItem = FastCombinationItem()
    override val gameSchedulers: FastCombinationScheduler = FastCombinationScheduler(this)

    private val productionItems = mutableMapOf<Material, UUID?>()
    private val productionItemsScoreBoard = ScoreBoardManager("craft-list")

    override suspend fun onStart() {
        participationPlayerList.forEach {
            productionItemsScoreBoard.visibleScoreboard(it)
            it.gameMode = GameMode.SURVIVAL
        }
        productionItems.clear()
        Combination.items.shuffled().subList(0, 10).forEach {
            productionItems[it.first] = null
        }
        refreshScoreboard()
    }

    override suspend fun onStop(rank: List<Player>) {
        BlockBreakEvent.getHandlerList().unregister(this)
        PlayerJoinEvent.getHandlerList().unregister(this)
        EntityDeathEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)
        participationPlayerList.forEach {
            productionItemsScoreBoard.invisibleScoreboard(it)
        }
    }

    @EventHandler
    fun onPlayerMiniGameDieEvent(event: PlayerMiniGameDieEvent) {
        val player = event.player
        player.syncTeleport(mapLocations.respawn)
        player.miniGameStatus = PlayerMiniGameStatus.ALIVE
    }

    private fun refreshScoreboard() {
        productionItemsScoreBoard.setBoardContent(
            productionItems.map {
                if (it.value == null) {
                    ChatColor.GRAY.toString() + "□ ${Combination.items.find { craftItem -> craftItem.first == it.key }?.second}"
                } else {
                    ChatColor.GREEN.toString() + "■ ${Combination.items.find { craftItem -> craftItem.first == it.key }?.second}"
                }
            }
        )
    }

    @EventHandler
    suspend fun onBlockBreakEvent(event: BlockBreakEvent) {
        val beforeType = event.block.type
        delay(50)
        event.block.type = beforeType
    }

    @EventHandler
    fun onEntityDeathEvent(event: EntityDeathEvent) {
        val entity = event.entity

        if (entity is Player) {
            return
        }

        entity.world.spawnEntity(entity.location, entity.type).customName = entity.type.toString()
    }

    @EventHandler
    suspend fun onCraftItemEvent(event: CraftItemEvent) {
        val currentItem = event.currentItem ?: return

        if (productionItems.contains(currentItem.type).not()) {
            return
        }

        val isAlreadyCreated = productionItems[currentItem.type] != null

        if (isAlreadyCreated) {
            return
        }

        productionItems[currentItem.type] = event.whoClicked.uniqueId
        event.whoClicked.location.world!!.spawn(event.whoClicked.location, Firework::class.java).apply {
            fireworkMeta = fireworkMeta.apply {
                addEffect(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.LIME).build())
                power = 0
            }
        }

        event.whoClicked.location.world!!.playSound(
            event.whoClicked.location,
            Sound.ENTITY_FIREWORK_ROCKET_BLAST,
            1f,
            1f
        )

        participationPlayerList.forEach { player ->
            player.sendMessage("누군가 아이템을 완성 하였습니다.")
        }

        refreshScoreboard()

        if (!gameStopCondition()) {
            return
        }

        val totalCountMap = participationPlayerList
            .associate { it.uniqueId to 0 }
            .toMutableMap()

        productionItems.values.filterNotNull().forEach {
            totalCountMap[it] = (totalCountMap[it] ?: 0) + 1
        }

        val resultMap = totalCountMap
            .toSortedMap(
                Comparator { o1: UUID?, o2: UUID? ->
                    return@Comparator (totalCountMap[o2] ?: 0) - (totalCountMap[o1] ?: 0)
                }
            ).keys
            .filterNotNull()
            .mapNotNull { Bukkit.getPlayer(it) }

        stopGame(resultMap)
    }

    override suspend fun gameStopCondition(): Boolean {
        return productionItems.values.count { it == null } == 0
    }

}