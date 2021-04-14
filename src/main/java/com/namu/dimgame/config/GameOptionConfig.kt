package com.namu.dimgame.config

import org.bukkit.configuration.serialization.ConfigurationSerializable

data class GameOptionConfig(
    val isBlockPlace: Boolean,
    val isBlockBreak: Boolean,
    val isCraft: Boolean,
    val isAttack: Boolean,
) : ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(data: Map<String, Any>): GameOptionConfig {
            return GameOptionConfig(
                data["isBlockPlace"] as Boolean,
                data["isBlockBreak"] as Boolean,
                data["isCraft"] as Boolean,
                data["isAttack"] as Boolean,
            )
        }
    }

    override fun serialize(): Map<String, Boolean> {
        return mapOf(
            "isBlockPlace" to isBlockPlace,
            "isBlockBreak" to isBlockBreak,
            "isCraft" to isCraft,
            "isAttack" to isAttack,
        )
    }
}

