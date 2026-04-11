package com.zoma1101.music_player.util;

public final class CombatPulseTracker {
    private final int pulseTicks;
    private int remainingTicks = 0;

    public CombatPulseTracker(int pulseTicks) {
        this.pulseTicks = Math.max(0, pulseTicks);
    }

    public void pulse() {
        remainingTicks = Math.max(remainingTicks, pulseTicks);
    }

    public boolean tickAndCheckActive() {
        if (remainingTicks <= 0) {
            return false;
        }
        remainingTicks--;
        return true;
    }

    public void reset() {
        remainingTicks = 0;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }
}
