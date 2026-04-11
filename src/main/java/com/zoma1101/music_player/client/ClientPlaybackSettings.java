package com.zoma1101.music_player.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.zoma1101.music_player.Music_Player;
import com.zoma1101.music_player.client.overlay.NowPlayingOverlayStyle;
import com.zoma1101.music_player.client.overlay.RgbaColor;
import com.zoma1101.music_player.client.overlay.UIAnchor;
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
        return current.overlayStyle.enabled;
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
        current.overlayStyle.enabled = value;
    }

    public static NowPlayingOverlayStyle getOverlayStyle() {
        return current.overlayStyle.toStyle();
    }

    public static void setOverlayStyle(NowPlayingOverlayStyle style) {
        NowPlayingOverlayStyle safe = style == null ? NowPlayingOverlayStyle.createDefault() : style.copy();
        safe.sanitize();
        current.overlayStyle = OverlayStyleData.fromStyle(safe);
    }

    private static PlaybackSettingsData sanitize(PlaybackSettingsData source) {
        PlaybackSettingsData safe = PlaybackSettingsData.createDefault();
        if (source == null) {
            return safe;
        }

        safe.musicVolume = clampVolume(source.musicVolume);
        safe.fadeInTicks = Math.max(0, source.fadeInTicks);
        safe.fadeOutTicks = Math.max(0, source.fadeOutTicks);
        safe.overlayStyle = OverlayStyleData.sanitize(source.overlayStyle);

        if (source.legacyShowNowPlayingHud != null) {
            safe.overlayStyle.enabled = source.legacyShowNowPlayingHud;
        }
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

        @SerializedName("overlay_style")
        private OverlayStyleData overlayStyle = OverlayStyleData.createDefault();

        @SerializedName("showNowPlayingHud")
        private Boolean legacyShowNowPlayingHud = null;

        private static PlaybackSettingsData createDefault() {
            return new PlaybackSettingsData();
        }
    }

    private static final class OverlayStyleData {
        private boolean enabled = true;
        private String anchor = NowPlayingOverlayStyle.DEFAULT_ANCHOR.name();
        private int offsetX = NowPlayingOverlayStyle.DEFAULT_OFFSET_X;
        private int offsetY = NowPlayingOverlayStyle.DEFAULT_OFFSET_Y;
        private int textColorRgba = NowPlayingOverlayStyle.DEFAULT_TEXT_COLOR_RGBA;
        private boolean showBackground = false;
        private int backgroundColorRgba = NowPlayingOverlayStyle.DEFAULT_BACKGROUND_COLOR_RGBA;
        private int displayDurationTicks = NowPlayingOverlayStyle.DEFAULT_DISPLAY_DURATION_TICKS;
        private int enterDurationTicks = NowPlayingOverlayStyle.DEFAULT_ENTER_DURATION_TICKS;
        private int exitDurationTicks = NowPlayingOverlayStyle.DEFAULT_EXIT_DURATION_TICKS;
        private int animationDistancePx = NowPlayingOverlayStyle.DEFAULT_ANIMATION_DISTANCE_PX;
        private boolean textShadow = true;

        private static OverlayStyleData createDefault() {
            return new OverlayStyleData();
        }

        private static OverlayStyleData sanitize(OverlayStyleData source) {
            OverlayStyleData safe = createDefault();
            if (source == null) {
                return safe;
            }

            safe.enabled = source.enabled;
            safe.anchor = UIAnchor.fromString(source.anchor, NowPlayingOverlayStyle.DEFAULT_ANCHOR).name();
            safe.offsetX = source.offsetX;
            safe.offsetY = source.offsetY;
            safe.textColorRgba = RgbaColor.sanitize(source.textColorRgba);
            safe.showBackground = source.showBackground;
            safe.backgroundColorRgba = RgbaColor.sanitize(source.backgroundColorRgba);
            safe.displayDurationTicks = Math.max(0, source.displayDurationTicks);
            safe.enterDurationTicks = Math.max(0, source.enterDurationTicks);
            safe.exitDurationTicks = Math.max(0, source.exitDurationTicks);
            safe.animationDistancePx = Math.max(0, source.animationDistancePx);
            safe.textShadow = source.textShadow;
            return safe;
        }

        private static OverlayStyleData fromStyle(NowPlayingOverlayStyle style) {
            OverlayStyleData data = createDefault();
            data.enabled = style.isEnabled();
            data.anchor = style.getAnchor().name();
            data.offsetX = style.getOffsetX();
            data.offsetY = style.getOffsetY();
            data.textColorRgba = style.getTextColorRgba();
            data.showBackground = style.isShowBackground();
            data.backgroundColorRgba = style.getBackgroundColorRgba();
            data.displayDurationTicks = style.getDisplayDurationTicks();
            data.enterDurationTicks = style.getEnterDurationTicks();
            data.exitDurationTicks = style.getExitDurationTicks();
            data.animationDistancePx = style.getAnimationDistancePx();
            data.textShadow = style.isTextShadow();
            return data;
        }

        private NowPlayingOverlayStyle toStyle() {
            NowPlayingOverlayStyle style = NowPlayingOverlayStyle.createDefault();
            style.setEnabled(enabled);
            style.setAnchor(UIAnchor.fromString(anchor, NowPlayingOverlayStyle.DEFAULT_ANCHOR));
            style.setOffsetX(offsetX);
            style.setOffsetY(offsetY);
            style.setTextColorRgba(textColorRgba);
            style.setShowBackground(showBackground);
            style.setBackgroundColorRgba(backgroundColorRgba);
            style.setDisplayDurationTicks(displayDurationTicks);
            style.setEnterDurationTicks(enterDurationTicks);
            style.setExitDurationTicks(exitDurationTicks);
            style.setAnimationDistancePx(animationDistancePx);
            style.setTextShadow(textShadow);
            style.sanitize();
            return style;
        }
    }
}
