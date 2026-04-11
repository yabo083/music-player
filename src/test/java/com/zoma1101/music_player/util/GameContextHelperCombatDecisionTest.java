package com.zoma1101.music_player.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameContextHelperCombatDecisionTest {

    @Test
    void hostileMobRecentlyHitByPlayerTriggersCombatEvenIfNotAggressiveFlagged() {
        boolean result = GameContextHelper.shouldTreatAsCombatTarget(
                true,
                false,
                false,
                false,
                true,
                true,
                false
        );

        assertTrue(result);
    }

    @Test
    void passiveMobRecentlyHitByPlayerDoesNotTriggerCombat() {
        boolean result = GameContextHelper.shouldTreatAsCombatTarget(
                false,
                false,
                false,
                false,
                false,
                true,
                false
        );

        assertFalse(result);
    }

    @Test
    void hostileMobWithoutAnyCombatRelationDoesNotTriggerCombat() {
        boolean result = GameContextHelper.shouldTreatAsCombatTarget(
                true,
                false,
                true,
                false,
                false,
                false,
                false
        );

        assertFalse(result);
    }
}
