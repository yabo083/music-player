package com.zoma1101.music_player.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatPulseTrackerTest {

    @Test
    void pulseKeepsTrackerActiveForConfiguredTicks() {
        CombatPulseTracker tracker = new CombatPulseTracker(3);
        tracker.pulse();

        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());
        assertFalse(tracker.tickAndCheckActive());
    }

    @Test
    void pulseRefreshesRemainingTicks() {
        CombatPulseTracker tracker = new CombatPulseTracker(4);
        tracker.pulse();

        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());

        tracker.pulse();

        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());
        assertTrue(tracker.tickAndCheckActive());
        assertFalse(tracker.tickAndCheckActive());
    }
}
