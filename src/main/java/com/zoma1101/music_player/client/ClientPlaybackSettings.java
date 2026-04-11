package com.zoma1101.music_player.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.zoma1101.music_player.Music_Player;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ClientPlaybackSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve(Music_Player.MOD_ID + "_client_settings.json");

    private static final float DEFAULT_MUSIC_VOLUME = 1.0f;
    private static final int DEFAULT_FADE_IN_TICKS = 20;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;
    private static final boolean DEFAULT_SHOW_NOW_PLAYING = true;

    private static PlaybackSettingsData current = PlaybackSettingsData.createDefault();

    static {
        load();
    }

    private ClientPlaybackSettings() {
    }

    public static void load() {
        current = PlaybackSettingsData.createDefault();

        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            PlaybackSettingsData loaded = GSON.fromJson(reader, PlaybackSettingsData.class);
            if (loaded == null) {
                LOGGER.warn("Client playback settings file was empty. Using defaults.");
                return;
            }
            current = sanitize(loaded);
        } catch (IOException | JsonParseException e) {
            LOGGER.error("Failed to load client playback settings from {}.", CONFIG_FILE.toAbsolutePath(), e);
        }
    }

    public static void save() {
        current = sanitize(current);
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save client playback settings to {}.", CONFIG_FILE.toAbsolutePath(), e);
        }
    }

    public static float getMusicVolume() {
        return current.musicVolume;
    }

    public static int getFadeInTicks() {
        return current.fadeInTicks;
    }

    public static int getFadeOutTicks() {
        return current.fadeOutTicks;
    }

    public static boolean shouldShowNowPlayingHud() {
        return current.showNowPlayingHud;
    }

    public static void setMusicVolume(float value) {
        current.musicVolume = clampVolume(value);
    }

    public static void setFadeInTicks(int value) {
        current.fadeInTicks = Math.max(0, value);
    }

    public static void setFadeOutTicks(int value) {
        current.fadeOutTicks = Math.max(0, value);
    }

    public static void setShowNowPlayingHud(boolean value) {
        current.showNowPlayingHud = value;
    }

    private static PlaybackSettingsData sanitize(PlaybackSettingsData source) {
        PlaybackSettingsData safe = PlaybackSettingsData.createDefault();
        if (source == null) {
            return safe;
        }
        safe.musicVolume = clampVolume(source.musicVolume);
        safe.fadeInTicks = Math.max(0, source.fadeInTicks);
        safe.fadeOutTicks = Math.max(0, source.fadeOutTicks);
        safe.showNowPlayingHud = source.showNowPlayingHud;
        return safe;
    }

    private static float clampVolume(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return DEFAULT_MUSIC_VOLUME;
        }
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static final class PlaybackSettingsData {
        private float musicVolume = DEFAULT_MUSIC_VOLUME;
        private int fadeInTicks = DEFAULT_FADE_IN_TICKS;
        private int fadeOutTicks = DEFAULT_FADE_OUT_TICKS;
        private boolean showNowPlayingHud = DEFAULT_SHOW_NOW_PLAYING;

        private static PlaybackSettingsData createDefault() {
            return new PlaybackSettingsData();
        }
    }
}
