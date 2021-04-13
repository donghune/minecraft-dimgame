package com.namu.dimgame.entity.config

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.util.BoundingBox

data class MapLocationConfig(
    val boundingBox: BoundingBox,
    val spawnLocation: Location,
) : ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(data: Map<String, Any>): MapLocationConfig {
            return MapLocationConfig(
                data["boundingBox"] as BoundingBox,
                data["spawnLocation"] as Location,
            )
        }
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "boundingBox" to boundingBox,
            "spawnLocation" to spawnLocation,
        )
    }
}