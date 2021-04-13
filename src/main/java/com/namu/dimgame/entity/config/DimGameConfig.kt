package com.namu.dimgame.entity.config

abstract class DimGameConfig {
    abstract val gameBaseConfig: GameBaseConfig

    abstract fun create() : DimGameConfig
}