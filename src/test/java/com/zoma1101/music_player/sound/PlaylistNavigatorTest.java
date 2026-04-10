package com.zoma1101.music_player.sound;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PlaylistNavigatorTest {

    @Test
    void sequentialModeCyclesInArrayOrder() {
        MusicDefinition definition = createDefinition(List.of("a", "b", "c"), "sequential", null);
        PlaylistNavigator navigator = new PlaylistNavigator(new Random(1L));

        assertEquals("a", navigator.nextTrackKey(definition, true));
        assertEquals("b", navigator.nextTrackKey(definition, false));
        assertEquals("c", navigator.nextTrackKey(definition, false));
        assertEquals("a", navigator.nextTrackKey(definition, false));
    }

    @Test
    void specifiedModeUsesPlayOrderField() {
        MusicDefinition definition = createDefinition(List.of("a", "b", "c"), "specified", List.of("2", "0", "1"));
        PlaylistNavigator navigator = new PlaylistNavigator(new Random(1L));

        assertEquals("c", navigator.nextTrackKey(definition, true));
        assertEquals("a", navigator.nextTrackKey(definition, false));
        assertEquals("b", navigator.nextTrackKey(definition, false));
        assertEquals("c", navigator.nextTrackKey(definition, false));
    }

    @Test
    void randomModeAvoidsImmediateRepeatsWhenMoreThanOneTrack() {
        MusicDefinition definition = createDefinition(List.of("a", "b", "c"), "random", null);
        PlaylistNavigator navigator = new PlaylistNavigator(new Random(7L));

        String previous = null;
        for (int i = 0; i < 20; i++) {
            String current = navigator.nextTrackKey(definition, i == 0);
            if (previous != null) {
                assertNotEquals(previous, current);
            }
            previous = current;
        }
    }

    private static MusicDefinition createDefinition(List<String> keys, String mode, List<String> playOrder) {
        MusicDefinition definition = new MusicDefinition();
        definition.playMode = mode;
        definition.playOrder = playOrder == null ? null : new ArrayList<>(playOrder);
        definition.musicFilesInPack = new ArrayList<>(keys);
        definition.setSoundEventKeys(new ArrayList<>(keys));
        return definition;
    }
}
