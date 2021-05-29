package com.namu.dimgame.minigame.spleef


import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import com.namu.namulibrary.extension.sendInfoMessage
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random

class Spleef : DimGame<SpleefItem, SpleefSchedulers>() {

    override val name: String = ChatColor.RED.toString() + "스플리프"
    override val description: String =
        "한 겹으로 2층에 걸쳐 쌓여있는 사각형 눈블럭 맵에서 플레이어들은 서로를 떨어뜨려야만 하는 게임입니다.\n가장 오래 살아남았던 3등까지 카운트 됩니다."

    override val mapLocations: DimGameMap = DimGameMap(
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

    override val gameItems: SpleefItem = SpleefItem()
    override val gameSchedulers: SpleefSchedulers = SpleefSchedulers(this)
    override val defaultItems: List<ItemStack> = listOf(
        gameItems.getItemById(SpleefItem.Code.SHOVEL)
    )

    override fun onStart() {
    }

    override fun onStop(rank: List<Player>) {
        BlockBreakEvent.getHandlerList().unregister(this)
        PlayerInteractEvent.getHandlerList().unregister(this)

        // 파괴된 블럭 원복
        breakBlockList.forEach {
            Bukkit.getWorld("world")!!.getBlockAt(it.location).type = Material.SNOW_BLOCK
        }

        if (rank.isNotEmpty()) {
            Bukkit.getOnlinePlayers().forEach {
                it.sendTitle("", "${rank[0].displayName} is Winner", 10, 20, 10)
            }
        }
    }

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {

        when (playerState) {
            PlayerStatus.ALIVE -> {
            }
            PlayerStatus.DIE -> {
                if (player.gameMode == GameMode.SPECTATOR) {
                    player.teleport(mapLocations.respawn)
                    return
                }

                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)

                Bukkit.getOnlinePlayers().forEach { it.sendInfoMessage("${player.displayName}님이 탈락하셨습니다.") }

                if (finishedPlayerList.contains(player.uniqueId)) {
                    return
                }

                finishedPlayerList.add(player.uniqueId)

                if (alivePlayers.size == 1) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopGame(finishedPlayerList.map { Bukkit.getPlayer(it)!! }.reversed())
                }
            }
        }

    }

    override fun gameStopCondition(): Boolean {
        return false
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
