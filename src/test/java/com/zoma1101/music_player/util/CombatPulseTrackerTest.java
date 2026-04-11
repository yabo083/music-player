package com.zoma1101.music_player.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatPulseTrackerTest {

    @Test
    void queryDoesNotConsumePulseTicks() {
        CombatPulseTracker tracker = new CombatPulseTracker(3);
        tracker.pulse();

        for (int i = 0; i < 10; i++) {
            assertTrue(tracker.isActive());
        }
    }

    @Test
    void pulseExpiresOnlyOnTickProgress() {
        CombatPulseTracker tracker = new CombatPulseTracker(4);
        tracker.pulse();

        assertTrue(tracker.isActive());
        tracker.onClientTick();
        tracker.onClientTick();
        tracker.onClientTick();
        assertTrue(tracker.isActive());
        tracker.onClientTick();
        assertFalse(tracker.isActive());
    }

    @Test
    void pulseRefreshesRemainingTicks() {
        CombatPulseTracker tracker = new CombatPulseTracker(4);
        tracker.pulse();
        tracker.onClientTick();
        tracker.onClientTick();
        assertTrue(tracker.isActive());

        tracker.pulse();
        tracker.onClientTick();
        tracker.onClientTick();
        tracker.onClientTick();
        assertTrue(tracker.isActive());
        tracker.onClientTick();
        assertFalse(tracker.isActive());
    }

    @Test
    void resetClearsPulseState() {
        CombatPulseTracker tracker = new CombatPulseTracker(8);
        tracker.pulse();
        assertTrue(tracker.isActive());

        tracker.reset();
        assertFalse(tracker.isActive());
    }
}
