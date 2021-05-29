package com.namu.dimgame.minigame.fast_combination

import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import com.namu.dimgame.util.ScoreBoardManager
import com.namu.namulibrary.schedular.SchedulerManager
import org.bukkit.*
import org.bukkit.entity.Firework
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
    override val name: String = ChatColor.GOLD.toString() + "빨리 조합하기"
    override val description: String = "하세요!"

    override val mapLocations: DimGameMap = DimGameMap(
        Location(Bukkit.getWorld("world"), 336.0, 118.0, 44.0),
        Location(Bukkit.getWorld("world"), 421.0, 80.0, 130.0),
        Location(Bukkit.getWorld("world"), 380.0, 99.0, 96.0),
    )

    private val craftItemList = listOf(
        Material.CRAFTING_TABLE to "조합대",
        Material.STICK to "막대기",
        Material.WOODEN_AXE to "나무 도끼",
        Material.WOODEN_HOE to "나무 괭이",
        Material.WOODEN_PICKAXE to "나무 곡괭이",
        Material.WOODEN_SHOVEL to "나무 삽",
        Material.WOODEN_SWORD to "나무 검",
        Material.STONE_AXE to "돌 도끼",
        Material.STONE_HOE to "돌 괭이",
        Material.STONE_PICKAXE to "돌 곡괭이",
        Material.STONE_SHOVEL to "돌 삽",
        Material.STONE_SWORD to "돌 검",
        Material.IRON_AXE to "철 도끼",
        Material.IRON_HOE to "철 괭이",
        Material.IRON_PICKAXE to "철 곡괭이",
        Material.IRON_SHOVEL to "철 삽",
        Material.IRON_SWORD to "철 검",
        Material.GOLDEN_AXE to "금 도끼",
        Material.GOLDEN_HOE to "금 괭이",
        Material.GOLDEN_PICKAXE to "금 곡괭이",
        Material.GOLDEN_SHOVEL to "금 삽",
        Material.GOLDEN_SWORD to "금 검",
        Material.DIAMOND_AXE to "다이아몬드 도끼",
        Material.DIAMOND_HOE to "다이아몬드 괭이",
        Material.DIAMOND_PICKAXE to "다이아몬드 곡괭이",
        Material.DIAMOND_SHOVEL to "다이아몬드 삽",
        Material.DIAMOND_SWORD to "다이아몬드 검",
        Material.STONE_BRICKS to "돌 벽돌",
        Material.CHEST to "상자",
        Material.NOTE_BLOCK to "노트 블럭",
        Material.CAKE to "케이크",
        Material.WHITE_BED to "하얀 침대",
        Material.SHIELD to "방패",
        Material.JACK_O_LANTERN to "잭 오 랜턴",
        Material.SHEARS to "가위",
        Material.RED_STAINED_GLASS_PANE to "빨간 색유리 판",
        Material.YELLOW_STAINED_GLASS_PANE to "노란 색유리 판",
        Material.IRON_DOOR to "철 문",
        Material.DARK_OAK_DOOR to "짙은 참나무 문",
        Material.OAK_PRESSURE_PLATE to "참나무 갑압판",
        Material.COOKIE to "쿠키",
        Material.GOLDEN_CARROT to "황금 당근",
        Material.RED_WOOL to "빨간 양털",
        Material.LEATHER_BOOTS to "가죽 장화",
        Material.DIAMOND_HELMET to "다이아몬드 헬멧",
        Material.BLUE_DYE to "파랑 염료",
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
            productionItems[it.first] = null
        }
        refreshScoreboard()
    }

    override fun onStop(rank: List<Player>) {
        BlockBreakEvent.getHandlerList().unregister(this)
        PlayerJoinEvent.getHandlerList().unregister(this)
        EntityDeathEvent.getHandlerList().unregister(this)
        CraftItemEvent.getHandlerList().unregister(this)
        participationPlayerList.forEach {
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
                    ChatColor.GRAY.toString() + "□ ${craftItemList.find { craftItem -> craftItem.first == it.key }?.second}"
                } else {
                    ChatColor.GREEN.toString() + "■ ${craftItemList.find { craftItem -> craftItem.first == it.key }?.second}"
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
            .map { it.uniqueId to 0 }
            .toMap()
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

    override fun gameStopCondition(): Boolean {
        return productionItems.values.count { it == null } == 0
    }

}