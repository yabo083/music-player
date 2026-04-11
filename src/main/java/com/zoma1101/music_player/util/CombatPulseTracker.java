package com.zoma1101.music_player.util;

public final class CombatPulseTracker {
    private final int pulseTicks;
    private int remainingTicks = 0;

    public CombatPulseTracker(int pulseTicks) {
        this.pulseTicks = Math.max(0, pulseTicks);
    }

    public synchronized void pulse() {
        remainingTicks = Math.max(remainingTicks, pulseTicks);
    }

    public synchronized void onClientTick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public synchronized boolean isActive() {
        return remainingTicks > 0;
    }

    public synchronized boolean tickAndCheckActive() {
        onClientTick();
        return isActive();
    }

    public synchronized void reset() {
        remainingTicks = 0;
    }

    public synchronized int getRemainingTicks() {
        return remainingTicks;
    }
}
