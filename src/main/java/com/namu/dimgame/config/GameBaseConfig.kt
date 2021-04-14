package com.namu.dimgame.config

import org.bukkit.configuration.serialization.ConfigurationSerializable

data class GameBaseConfig(
    val name: String,
    val description: String,
    val mapLocations: MapLocationConfig,
    val gameOption: GameOptionConfig,
) : ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(data: Map<String, Any>): GameBaseConfig {
            return GameBaseConfig(
                data["name"] as String,
                data["description"] as String,
                data["mapLocations"] as MapLocationConfig,
                data["gameOption"] as GameOptionConfig,
            )
        }
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "description" to description,
            "mapLocations" to mapLocations,
            "gameOption" to gameOption,
        )
    }
}