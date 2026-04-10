package com.zoma1101.music_player.sound;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class PlaylistNavigator {
    private final Random random;
    @Nullable
    private MusicDefinition currentDefinition = null;
    private int sequenceCursor = -1;
    private int lastRandomTrackIndex = -1;

    public PlaylistNavigator() {
        this(new Random());
    }

    public PlaylistNavigator(Random random) {
        this.random = random;
    }

    public void reset() {
        currentDefinition = null;
        sequenceCursor = -1;
        lastRandomTrackIndex = -1;
    }

    @Nullable
    public String nextTrackKey(@Nullable MusicDefinition definition, boolean forceReset) {
        if (definition == null) {
            reset();
            return null;
        }

        if (forceReset || currentDefinition != definition) {
            currentDefinition = definition;
            sequenceCursor = -1;
            lastRandomTrackIndex = -1;
        }

        List<String> soundEventKeys = definition.getSoundEventKeys();
        if (soundEventKeys.isEmpty()) {
            return null;
        }

        return switch (definition.getPlaybackMode()) {
            case RANDOM -> nextRandomTrack(soundEventKeys);
            case SPECIFIED -> nextSpecifiedTrack(soundEventKeys, definition.resolvePlaybackOrderIndices());
            case SEQUENTIAL -> nextSequentialTrack(soundEventKeys);
        };
    }

    private String nextSequentialTrack(List<String> soundEventKeys) {
        sequenceCursor = (sequenceCursor + 1) % soundEventKeys.size();
        return soundEventKeys.get(sequenceCursor);
    }

    @Nullable
    private String nextSpecifiedTrack(List<String> soundEventKeys, List<Integer> orderIndices) {
        if (orderIndices.isEmpty()) {
            return null;
        }
        sequenceCursor = (sequenceCursor + 1) % orderIndices.size();
        int actualIndex = orderIndices.get(sequenceCursor);
        if (actualIndex < 0 || actualIndex >= soundEventKeys.size()) {
            return null;
        }
        return soundEventKeys.get(actualIndex);
    }

    private String nextRandomTrack(List<String> soundEventKeys) {
        if (soundEventKeys.size() == 1) {
            lastRandomTrackIndex = 0;
            return soundEventKeys.get(0);
        }

        int nextIndex = random.nextInt(soundEventKeys.size());
        if (nextIndex == lastRandomTrackIndex) {
            nextIndex = (nextIndex + 1 + random.nextInt(soundEventKeys.size() - 1)) % soundEventKeys.size();
        }
        lastRandomTrackIndex = nextIndex;
        return soundEventKeys.get(nextIndex);
    }
}
