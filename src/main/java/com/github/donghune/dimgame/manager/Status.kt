package com.github.donghune.dimgame.manager

enum class PlayerMiniGameStatus {
    ALIVE, DIE;
}

enum class RoundGameStatus {
    WAITING, RUNNING
}

enum class GameStatus {
    NOT_PLAYING, PLAYING,
}

enum class ParticipantStatus {
    PARTICIPANT, OBSERVER
}