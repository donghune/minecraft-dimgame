package com.namu.dimgame.minigame.battle_of_push

import com.namu.dimgame.manager.PlayerStatus
import com.namu.dimgame.minigame.*


import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random

class BattleOfPush : DimGame<BattleOfPushItem, BattleOfPushScheduler>() {
    override val name: String = "밀치기 전투"
    override val description: String = "동그란 섬 밖으로 상대를 내보내세요!"
    override val mapLocations: DimGameMap = DimGameMap(
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
    override val gameItems: BattleOfPushItem = BattleOfPushItem()
    override val gameSchedulers: BattleOfPushScheduler = BattleOfPushScheduler(this)
    override val defaultItems: List<ItemStack> = listOf(gameItems.getItemById(BattleOfPushItem.Code.STICK))

    private val finishedPlayerList = mutableListOf<UUID>()

    override fun onStart() {
        participationPlayerList.forEach {
            it.teleport(
                mapLocations.respawn.apply {
                    x += Random.nextInt(-6, 6)
                    z += Random.nextInt(-6, 6)
                }
            )
        }

        gameSchedulers.getScheduler(BattleOfPushScheduler.Code.RANDOM_ITEM).runSecond(8, Int.MAX_VALUE)
    }

    override fun onStop(rank: List<Player>) {
        PlayerInteractEvent.getHandlerList().unregister(this)
        gameSchedulers.getScheduler(BattleOfPushScheduler.Code.RANDOM_ITEM).stopScheduler()
    }

    override fun onChangedPlayerState(player: Player, playerState: PlayerStatus) {
        when (playerState) {
            PlayerStatus.ALIVE -> {

            }
            PlayerStatus.DIE -> {
                player.gameMode = GameMode.SPECTATOR
                player.teleport(mapLocations.respawn)
                if (!finishedPlayerList.contains(player.uniqueId)) {
                    finishedPlayerList.add(player.uniqueId)
                }
                if (gameStopCondition()) {
                    finishedPlayerList.add(alivePlayers[0].uniqueId)
                    stopGame(finishedPlayerList.apply { reverse() }.map { Bukkit.getPlayer(it)!! }.toList())
                }
            }
        }
    }

    override fun gameStopCondition(): Boolean {
        return alivePlayers.size == 1
    }

}