package com.zoma1101.music_player.sound;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicDefinitionCompatibilityTest {

    private static final Gson GSON = new Gson();

    @Test
    void supportsLegacyCombatNightVillageFieldAliases() {
        String json = """
                {
                  "priority": 10,
                  "combat": true,
                  "night": false,
                  "village": true,
                  "music": "music/lifeline.ogg"
                }
                """;

        MusicDefinition definition = GSON.fromJson(json, MusicDefinition.class);
        assertNotNull(definition);
        assertEquals(Boolean.TRUE, definition.isCombat());
        assertEquals(Boolean.FALSE, definition.isNight());
        assertEquals(Boolean.TRUE, definition.isVillage());
        assertTrue(definition.getMusicFilesInPack().contains("music/lifeline.ogg"));
    }
}
