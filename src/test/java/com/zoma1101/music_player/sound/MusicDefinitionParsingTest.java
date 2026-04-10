package com.zoma1101.music_player.sound;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MusicDefinitionParsingTest {

    private final Gson gson = new GsonBuilder().create();

    @Test
    void parsesMusicFromSingleString() {
        String json = """
                {
                  "priority": 2,
                  "music": "music/lifeline.ogg"
                }
                """;

        MusicDefinition definition = gson.fromJson(json, MusicDefinition.class);

        assertEquals(List.of("music/lifeline.ogg"), definition.getMusicFilesInPack());
        assertEquals(MusicDefinition.PlaybackMode.SEQUENTIAL, definition.getPlaybackMode());
    }

    @Test
    void parsesMusicFromArray() {
        String json = """
                {
                  "priority": 2,
                  "music": ["music/flower_dance.ogg", "music/winter.ogg"],
                  "play_mode": "random"
                }
                """;

        MusicDefinition definition = gson.fromJson(json, MusicDefinition.class);

        assertEquals(List.of("music/flower_dance.ogg", "music/winter.ogg"), definition.getMusicFilesInPack());
        assertEquals(MusicDefinition.PlaybackMode.RANDOM, definition.getPlaybackMode());
    }

    @Test
    void resolvesSpecifiedOrderFromPlayOrderField() {
        String json = """
                {
                  "priority": 2,
                  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
                  "play_mode": "specified",
                  "play_order": ["music/c.ogg", "0"]
                }
                """;

        MusicDefinition definition = gson.fromJson(json, MusicDefinition.class);

        assertEquals(List.of(2, 0, 1), definition.resolvePlaybackOrderIndices());
    }

    @Test
    void fallsBackToSequentialWhenSpecifiedOrderIsInvalid() {
        String json = """
                {
                  "priority": 2,
                  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
                  "play_mode": "specified",
                  "play_order": ["not_exists"]
                }
                """;

        MusicDefinition definition = gson.fromJson(json, MusicDefinition.class);

        assertEquals(List.of(0, 1, 2), definition.resolvePlaybackOrderIndices());
    }
}
