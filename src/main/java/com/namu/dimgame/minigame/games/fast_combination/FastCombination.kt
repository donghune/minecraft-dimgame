package com.namu.dimgame.minigame.games.fast_combination

import com.namu.dimgame.minigame.*
import com.namu.dimgame.util.ScoreBoardManager
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.Comparator

class FastCombination : DimGame<FastCombinationItem, FastCombinationScheduler>() {
    override val name: String = "빨리 조합하기"
    override val description: String = "하세요!"

    override val mapLocations: DimGameMap = DimGameMap(
            Location(Bukkit.getWorld("world"), 336.0, 118.0, 44.0),
            Location(Bukkit.getWorld("world"), 421.0, 80.0, 130.0),
            Location(Bukkit.getWorld("world"), 380.0, 99.0, 96.0),
    )

    private val craftItemList = listOf(
            Material.CRAFTING_TABLE,
            Material.STICK,
            Material.WOODEN_AXE,
            Material.WOODEN_HOE,
            Material.WOODEN_PICKAXE,
            Material.WOODEN_SHOVEL,
            Material.WOODEN_SWORD,
            Material.STONE_AXE,
            Material.STONE_HOE,
            Material.STONE_PICKAXE,
            Material.STONE_SHOVEL,
            Material.STONE_SWORD,
            Material.IRON_AXE,
            Material.IRON_HOE,
            Material.IRON_PICKAXE,
            Material.IRON_SHOVEL,
            Material.IRON_SWORD,
            Material.GOLDEN_AXE,
            Material.GOLDEN_HOE,
            Material.GOLDEN_PICKAXE,
            Material.GOLDEN_SHOVEL,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_AXE,
            Material.DIAMOND_HOE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_SWORD
    )
    private val productionItems = mutableMapOf<Material, UUID?>()
    private val productionItemsScoreBoard = ScoreBoardManager("craft-list")

    override val gameOption: DimGameOption = DimGameOption(
            isBlockPlace = false,
            isBlockBreak = true,
            isCraft = true,
            isAttack = false
    )
    override val gameItems: FastCombinationItem = FastCombinationItem()
    override val gameSchedulers: FastCombinationScheduler = FastCombinationScheduler(this)
    override val defaultItems: List<ItemStack> = emptyList()

    override fun onStart() {
        participationPlayerList.forEach {
            it.teleport(mapLocations.respawn)
            it.gameMode = GameMode.SURVIVAL
            productionItemsScoreBoard.visibleScoreboard(it)
        }
        productionItems.clear()
        craftItemList.shuffled().subList(0, 10).forEach {
            productionItems[it] = null
        }
        refreshScoreboard()
    }

    override fun onStop(rank: List<Player>) {
        BlockBreakEvent.getHandlerList().unregister(this)
        PlayerJoinEvent.getHandlerList().unregister(this)
        EntityDeathEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)
        participationPlayerList.forEach {
            it.gameMode = GameMode.SURVIVAL
            productionItemsScoreBoard.invisibleScoreboard(it)
        }
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.teleport(mapLocations.respawn)
            }
        }
    }

    private fun refreshScoreboard() {
        productionItemsScoreBoard.setBoardContent(
                productionItems.map {
                    if (it.value == null) {
                        ChatColor.GRAY.toString() + "□ ${it.key}"
                    } else {
                        ChatColor.GREEN.toString() + "■ ${it.key}"
                    }
                }
        )
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val beforeType = event.block.type
        SchedulerManager {
            finished {
                event.block.type = beforeType
            }
        }.runSecond(0L, 1)
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (participationPlayerList.map { it.uniqueId }.contains(event.player.uniqueId)) {
            productionItemsScoreBoard.visibleScoreboard(event.player)
        }
    }

    @EventHandler
    fun onEntityDeathEvent(event: EntityDeathEvent) {
        if (event.entity is Player) {
            return
        }
        SchedulerManager {
            finished {
                event.entity.world.spawnEntity(event.entity.location, event.entity.type).customName =
                        event.entity.type.toString()
            }
        }.runSecond(0L, 1)
    }

    @EventHandler
    fun onCraftItemEvent(event: CraftItemEvent) {
        val currentItem = event.currentItem ?: return

        if (productionItems.contains(currentItem.type).not()) {
            return
        }

        val isAlreadyCreated = productionItems[currentItem.type] != null

        if (isAlreadyCreated) {
            return
        }

        productionItems[currentItem.type] = event.whoClicked.uniqueId
        participationPlayerList.forEach { player ->
            player.sendMessage("누군가 아이템을 완성 하였습니다.")
        }

        refreshScoreboard()

        if (!gameStopCondition()) {
            return
        }

        val resultMap = productionItems.values
                .groupingBy { uuid -> uuid }
                .eachCount()

        resultMap.toSortedMap(
                Comparator { o1: UUID?, o2: UUID? ->
                    return@Comparator (resultMap[o2] ?: 0) - (resultMap[o1] ?: 0)
                }
        )

        stopGame(resultMap.keys.filterNotNull().mapNotNull { Bukkit.getPlayer(it) })
    }

    override fun gameStopCondition(): Boolean {
        return productionItems.values.count { it == null } == 0
    }

}