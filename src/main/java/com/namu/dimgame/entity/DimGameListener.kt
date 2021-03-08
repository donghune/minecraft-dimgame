package com.namu.dimgame.entity

import org.bukkit.entity.Player

interface DimGameListener {

    fun onPrepare()
    fun onStart()
    fun onStop(rank: List<Player>)
    fun onChangedPlayerState(player: Player)

}