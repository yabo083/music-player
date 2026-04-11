package com.zoma1101.music_player.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStateTrackerTest {

    @Test
    void remainsInCombatForConfiguredGraceTicksAfterTargetsDisappear() {
        CombatStateTracker tracker = new CombatStateTracker(2);

        assertTrue(tracker.update(Set.of(101)));
        assertTrue(tracker.update(Collections.emptySet()));
        assertTrue(tracker.update(Collections.emptySet()));
        assertFalse(tracker.update(Collections.emptySet()));
    }

    @Test
    void freshCombatSignalResetsGraceWindow() {
        CombatStateTracker tracker = new CombatStateTracker(2);

        assertTrue(tracker.update(Set.of(101)));
        assertTrue(tracker.update(Collections.emptySet()));
        assertTrue(tracker.update(Set.of(202)));
        assertTrue(tracker.update(Collections.emptySet()));
        assertTrue(tracker.update(Collections.emptySet()));
        assertFalse(tracker.update(Collections.emptySet()));
    }
}
