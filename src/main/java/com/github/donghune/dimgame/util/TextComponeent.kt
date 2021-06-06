package com.github.donghune.dimgame.util

import com.github.donghune.namulibrary.extension.replaceChatColorCode
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getOnlinePlayers

fun error(message: String): String {
    return "&c[ERROR] &f$message".replaceChatColorCode()
}

fun debug(message: String): String {
    return "&e[Debug] &f$message".replaceChatColorCode()
}

fun info(message: String): String {
    return "&9[INFO] &f$message".replaceChatColorCode()
}

fun broadcastOnActionBar(message: String) {
    getOnlinePlayers().forEach {
        it.sendActionBar(Component.text(message.replaceChatColorCode()))
    }
}

fun broadcastOnTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    getOnlinePlayers().forEach {
        it.sendTitle(title.replaceChatColorCode(), subtitle.replaceChatColorCode(), fadeIn, stay, fadeOut)
    }
}