package com.github.donghune.dimgame.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class MiniGameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final List<UUID> rank;

    public MiniGameEndEvent(List<UUID> rank) {
        this.rank = rank;
    }

    public List<UUID> getRank() {
        return rank;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
