package com.namu.dimgame.game

import com.namu.dimgame.entity.*
import com.namu.dimgame.plugin
import com.namu.namulibrary.extension.ItemBuilder
import com.namu.namulibrary.extension.sendInfoMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class Spleef : DimGame() {

    override val name: String = "스플리프"
    override val description: String =
        "한 겹으로 2층에 걸쳐 쌓여있는 사각형 눈블럭 맵에서 플레이어들은 서로를 떨어뜨려야만 하는 게임입니다.\n가장 오래 살아남았던 3등까지 카운트 됩니다."
    override val gameType: GameType = GameType.RANK
    override val mapInfo: GameMap = GameMap(
        pos1 = Location(Bukkit.getWorld("world"), 481.0, 100.0, 370.0),
        pos2 = Location(Bukkit.getWorld("world"), 503.0, 30.0, 348.0),
        respawn = Location(Bukkit.getWorld("world"), 492.0, 94.0, 359.0)
    )

    override val gameOption: DimGameOption = DimGameOption(
        isBlockPlace = false,
        isBlockBreak = true,
        isCraft = false,
        isAttack = false,
    )

    override val defaultItems: List<ItemStack> = listOf(
        ItemBuilder().setMaterial(Material.DIAMOND_SHOVEL)
            .build()
            .apply { itemMeta.addEnchant(Enchantment.DIG_SPEED, 4, false) }
    )

    override val gameItems: List<ItemStack> = listOf(
        GAME_ITEM_INVISIBLE,
        GAME_ITEM_JUMP,
        GAME_ITEM_SPEED
    )

    companion object {
        private val GAME_ITEM_JUMP = ItemBuilder()
            .setMaterial(Material.STONE)
            .setDisplay("점프")
            .setLore(listOf("4칸 높이를 한 번에 점프합니다."))
            .build()

        private val GAME_ITEM_SPEED = ItemBuilder()
            .setMaterial(Material.STONE)
            .setDisplay("신속")
            .setLore(listOf("3초간 신속1이 부여됩니다."))
            .build()

        private val GAME_ITEM_INVISIBLE = ItemBuilder()
            .setMaterial(Material.STONE)
            .setDisplay("투명")
            .setLore(listOf("2초간 투명이 부여됩니다."))
            .build()
    }

    override fun onStart() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun onStop(rank: List<Player>) {
        Bukkit.broadcastMessage("rank = [${rank}]")

        BlockBreakEvent.getHandlerList().unregister(this)
        PlayerInteractEvent.getHandlerList().unregister(this)

        breakBlockList.forEach {
            Bukkit.getWorld("world")!!.getBlockAt(it.location).type = Material.SNOW_BLOCK
        }

        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle("", "${rank[0].displayName} is Winner", 10, 20, 10)
        }
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerState) {

        when (playerState) {
            PlayerState.ALIVE -> {
            }
            PlayerState.DIE -> {
                if (player.gameMode == GameMode.SPECTATOR) {
                    player.teleport(mapInfo.respawn)
                    return
                }

                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapInfo.respawn)

                Bukkit.getOnlinePlayers().forEach { it.sendInfoMessage("${player.displayName}님이 탈락하셨습니다.") }

                val aliveList = participationPlayerList.filter { getPlayerState(it) == PlayerState.ALIVE }.toList()
                val aliveCount = aliveList.count()

                if (aliveCount == 1) {
                    stopMiniGame(aliveList)
                }
            }
        }

    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val handItem = event.player.inventory.itemInMainHand

        if (handItem.type.isAir) {
            return
        }

        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        when {
            handItem.isSimilar(GAME_ITEM_INVISIBLE) -> {
                event.isCancelled = true
                handItem.amount -= 1
                event.player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 20 * 2, 1, false, false))
            }
            handItem.isSimilar(GAME_ITEM_JUMP) -> {
                event.isCancelled = true
                handItem.amount -= 1
                event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 20 * 2, 3, false, false))
            }
            handItem.isSimilar(GAME_ITEM_SPEED) -> {
                event.isCancelled = true
                handItem.amount -= 1
                event.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 3, 0, false, false))
            }
        }
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

        event.player.inventory.addItem(gameItems.random())
    }
}