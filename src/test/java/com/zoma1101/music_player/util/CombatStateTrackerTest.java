package com.zoma1101.music_player.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStateTrackerTest {

    @Test
    void graceWindowDecaysByElapsedTicksInsteadOfUpdateCalls() {
        CombatStateTracker tracker = new CombatStateTracker(80);

        assertTrue(tracker.update(Set.of(101), 1));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertFalse(tracker.update(Collections.emptySet(), 20));
    }

    @Test
    void freshCombatSignalResetsGraceWindowWithElapsedTicks() {
        CombatStateTracker tracker = new CombatStateTracker(60);

        assertTrue(tracker.update(Set.of(101), 1));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertTrue(tracker.update(Set.of(202), 1));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertTrue(tracker.update(Collections.emptySet(), 20));
        assertFalse(tracker.update(Collections.emptySet(), 20));
    }

    @Test
    void nonPositiveElapsedTicksAreTreatedAsSingleTick() {
        CombatStateTracker tracker = new CombatStateTracker(2);

        assertTrue(tracker.update(Set.of(101), 1));
        assertTrue(tracker.update(Collections.emptySet(), 0));
        assertFalse(tracker.update(Collections.emptySet(), -5));
    }
}
