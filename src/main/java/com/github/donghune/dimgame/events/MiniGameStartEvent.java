package com.github.donghune.dimgame.events;

import com.github.donghune.dimgame.minigame.MiniGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MiniGameStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final MiniGame miniGame;

    public MiniGameStartEvent(MiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public MiniGame getMiniGame() {
        return miniGame;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
