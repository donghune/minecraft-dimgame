package com.namu.dimgame.config

abstract class DimGameConfig {
    abstract val gameBaseConfig: GameBaseConfig

    abstract fun create() : DimGameConfig
}