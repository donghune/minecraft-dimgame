package com.namu.dimgame.game

import com.namu.dimgame.minigame.GameStatus
import com.namu.dimgame.plugin
import com.namu.dimgame.util.ScoreBoardManager
import com.namu.namulibrary.scoreboard.CustomScoreBoard
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import kotlin.random.Random

object DimGameManager : Listener, MiniGameLifeCycle() {

    // config
    val lobbyLocation: Location = Location(Bukkit.getWorld("world"), 232.0, 86.0, 262.0)

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (gameState != GameStatus.PLAYING) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (gameState == GameStatus.NOT_PLAYING) {
            event.player.teleport(lobbyLocation)
        }
    }

    override fun onStart() {

        // 게임을 시작하면 화이트리스트 설정
        Bukkit.getServer().setWhitelist(true)

        Bukkit.getOnlinePlayers().forEach { playerScoreMap[it] = 0 }
        Bukkit.getOnlinePlayers().forEach { updatePlayerScoreBoard(it) }

    }

    override fun onRoundStart() {
        // 미니게임 시작
    }

    override fun onRoundEnd(rank: List<UUID>) {
        // 플레이어의 랭크 처리
        rank.forEachIndexed { index, player ->
            if (index >= 3) {
                return@forEachIndexed
            }
            playerScoreMap[Bukkit.getPlayer(player)!!] = (playerScoreMap[Bukkit.getPlayer(player)!!] ?: 0) + (3 - index)
        }

        Bukkit.getOnlinePlayers().forEach { updatePlayerScoreBoard(it) }
    }

    override fun onStop() {

        // 유저들이 접속 할 수 있도록 화이트리스트 해제
        Bukkit.getServer().setWhitelist(false)

        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(
                "MVP",
                playerScoreMap.filter { info -> info.value == playerScoreMap.values.max()!! }.keys.first().displayName,
                10,
                60,
                10
            )
        }

        repeat(20) { _ ->
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable {
                    lobbyLocation.apply {
                        x += Random.nextInt(-10, 10)
                        y += Random.nextInt(-10, 10)
                        z += Random.nextInt(-10, 10)
                    }.also { location ->
                        location.world.spawn(location, Firework::class.java).apply {
                            fireworkMeta = fireworkMeta.apply {
                                addEffect(
                                    FireworkEffect.builder().with(FireworkEffect.Type.values().first())
                                        .withColor(
                                            Color.fromBGR(
                                                Random.nextInt(0, 255),
                                                Random.nextInt(0, 255),
                                                Random.nextInt(0, 255)
                                            )
                                        ).build()
                                )
                                power = 0
                            }
                        }
                        location.world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)
                    }
                },
                Random.nextLong(10, 40)
            )
        }

    }

    private val playerScoreMap = mutableMapOf<Player, Int>()

    private fun updatePlayerScoreBoard(player: Player) {
        val playerScoreBoard = ScoreBoardManager("Dim-Game")
        // Overall Ranking
        val boardContent = mutableListOf<String>()

        boardContent.add("내 정보")
        playerScoreMap[player].also {
            boardContent.add("총점 : ★ x $it")
            boardContent.add("")
        }

        // Overall Ranking
        boardContent.add("전체 순위")
        playerScoreMap
            .toSortedMap { player1, player2 ->
                return@toSortedMap if (playerScoreMap[player1] == playerScoreMap[player2]) {
                    -1
                } else {
                    (playerScoreMap[player2] ?: 0) - (playerScoreMap[player1] ?: 0)
                }
            }
            .map { it.key to it.value }
            .subList(0, 3)
            .forEachIndexed { index, pair ->
                boardContent.add("${index + 1}등 ${pair.first.displayName}")
            }
        playerScoreBoard.setBoardContent(boardContent)
        playerScoreBoard.visibleScoreboard(player)
    }

}