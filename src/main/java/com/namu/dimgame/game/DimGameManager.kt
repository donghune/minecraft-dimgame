package com.namu.dimgame.game

import com.namu.dimgame.minigame.GameStatus
import com.namu.dimgame.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

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

    }

    override fun onRoundStart() {
        // 미니게임 시작
    }

    override fun onRoundEnd(rank: List<UUID>) {
        // 플레이어의 랭크 처리
    }

    override fun onStop() {

        // 유저들이 접속 할 수 있도록 화이트리스트 해제
        Bukkit.getServer().setWhitelist(false)

    }

}