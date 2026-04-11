package com.zoma1101.music_player.util;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class CombatStateTracker {
    private final int graceTicks;
    private int remainingGraceTicks = 0;

    public CombatStateTracker(int graceTicks) {
        this.graceTicks = Math.max(0, graceTicks);
    }

    public boolean update(@Nullable Set<Integer> engagedMobIds) {
        if (engagedMobIds != null && !engagedMobIds.isEmpty()) {
            remainingGraceTicks = graceTicks;
            return true;
        }
        if (remainingGraceTicks > 0) {
            remainingGraceTicks--;
            return true;
        }
        return false;
    }

    public void reset() {
        remainingGraceTicks = 0;
    }
}
