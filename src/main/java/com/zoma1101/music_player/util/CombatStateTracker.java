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
        return update(engagedMobIds, 1);
    }

    public boolean update(@Nullable Set<Integer> engagedMobIds, int elapsedTicks) {
        int normalizedElapsedTicks = Math.max(1, elapsedTicks);
        if (engagedMobIds != null && !engagedMobIds.isEmpty()) {
            remainingGraceTicks = graceTicks;
            return true;
        }
        if (remainingGraceTicks > 0) {
            remainingGraceTicks = Math.max(0, remainingGraceTicks - normalizedElapsedTicks);
            if (remainingGraceTicks > 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    public int getRemainingGraceTicks() {
        return remainingGraceTicks;
    }

    public int getGraceTicks() {
        return graceTicks;
    }

    public boolean isInGrace() {
        return remainingGraceTicks > 0;
    }

    public void reset() {
        remainingGraceTicks = 0;
    }
}
