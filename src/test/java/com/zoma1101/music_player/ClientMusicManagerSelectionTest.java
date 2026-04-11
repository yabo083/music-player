package com.zoma1101.music_player;

import com.zoma1101.music_player.sound.MusicDefinition;
import com.zoma1101.music_player.util.MusicConditionEvaluator;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class ClientMusicManagerSelectionTest {

    @Test
    void prioritizesCombatDefinitionWhenInCombat() {
        MusicConditionEvaluator.CurrentContext context = new MusicConditionEvaluator.CurrentContext();
        context.isInCombat = true;
        context.isNight = true;

        MusicDefinition nightAmbient = createDefinition("night_ambient", 200, null, true);
        MusicDefinition combat = createDefinition("combat_theme", 50, true, null);

        MusicDefinition selected = ClientMusicManager.pickBestMatchingDefinition(List.of(nightAmbient, combat), context);

        assertSame(combat, selected);
    }

    @Test
    void fallsBackToAmbientWhenNoCombatDefinitionMatches() {
        MusicConditionEvaluator.CurrentContext context = new MusicConditionEvaluator.CurrentContext();
        context.isInCombat = true;
        context.isNight = true;

        MusicDefinition nightAmbient = createDefinition("night_ambient", 200, null, true);
        MusicDefinition combatButDayOnly = createDefinition("combat_day_only", 300, true, false);

        MusicDefinition selected = ClientMusicManager.pickBestMatchingDefinition(List.of(combatButDayOnly, nightAmbient), context);

        assertSame(nightAmbient, selected);
    }

    @Test
    void keepsAmbientSelectionWhenNotInCombat() {
        MusicConditionEvaluator.CurrentContext context = new MusicConditionEvaluator.CurrentContext();
        context.isInCombat = false;
        context.isNight = true;

        MusicDefinition nightAmbient = createDefinition("night_ambient", 200, null, true);
        MusicDefinition combat = createDefinition("combat_theme", 300, true, null);

        MusicDefinition selected = ClientMusicManager.pickBestMatchingDefinition(List.of(combat, nightAmbient), context);

        assertSame(nightAmbient, selected);
    }

    private static MusicDefinition createDefinition(String id, int priority, Boolean isCombat, Boolean isNight) {
        MusicDefinition definition = new MusicDefinition();
        definition.priority = priority;
        definition.musicFilesInPack = List.of("music/" + id + ".ogg");
        definition.isCombat = isCombat;
        definition.isNight = isNight;
        definition.setSoundPackId("test_pack");
        definition.setAbsoluteOggPaths(List.of(Path.of("dummy", id + ".ogg")));
        definition.setSoundEventKeys(List.of("test_pack/music/" + id));
        definition.setOggResourceLocations(List.of(
                ResourceLocation.fromNamespaceAndPath(Music_Player.MOD_ID, "test_pack/music/" + id)
        ));
        return definition;
    }
}
