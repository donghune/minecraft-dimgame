package com.namu.dimgame.entity

enum class PlayerState {
    ALIVE, DIE;
}

enum class MiniGameState {
    WAITING, RUNNING
}

enum class GameState {
    NOT_PLAYING, PLAYING, NEXT_WAITING,
}