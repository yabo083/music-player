package com.zoma1101.music_player.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FadingMusicStartupPolicyTest {

    @Test
    void allowsSilentStartWhenFadeInIsConfigured() {
        assertTrue(FadingMusicSoundInstance.shouldAllowSilentStart(20, 1.0f));
    }

    @Test
    void allowsSilentStartWhenTargetVolumeIsZero() {
        assertTrue(FadingMusicSoundInstance.shouldAllowSilentStart(0, 0.0f));
    }

    @Test
    void doesNotRequireSilentStartWhenFadeInIsDisabledAndVolumeIsPositive() {
        assertFalse(FadingMusicSoundInstance.shouldAllowSilentStart(0, 1.0f));
    }
}
