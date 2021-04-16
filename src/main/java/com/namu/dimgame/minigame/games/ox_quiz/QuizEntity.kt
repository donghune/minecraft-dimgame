package com.namu.dimgame.minigame.games.ox_quiz

import org.bukkit.entity.Player

class Quiz(
    private val content: String,
    val answer: Boolean
) {
    fun printQuizContent(player: Player) {
        player.sendMessage("========= [ QUIZ ] =========")
        player.sendMessage("")
        player.sendMessage("")
        player.sendMessage("Q. $content")
        player.sendMessage("")
        player.sendMessage("")
        player.sendMessage("========= [ QUIZ ] =========")
    }

    fun printQuizAnswer(player: Player) {
        player.sendMessage("========= [ QUIZ ] =========")
        player.sendMessage("")
        player.sendMessage("")
        player.sendMessage("A. $answer")
        player.sendMessage("")
        player.sendMessage("")
        player.sendMessage("========= [ QUIZ ] =========")
    }
}


val quizList = listOf<Quiz>(
    Quiz("true", true),
    Quiz("true", true),
    Quiz("true", true),
    Quiz("true", false),
    Quiz("true", true)
)