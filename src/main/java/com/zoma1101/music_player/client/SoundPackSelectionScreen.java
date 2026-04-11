package com.zoma1101.music_player.client;

import com.mojang.logging.LogUtils;
import com.zoma1101.music_player.ClientMusicManager;
import com.zoma1101.music_player.Music_Player;
import com.zoma1101.music_player.sound.SoundPackInfo;
import com.zoma1101.music_player.sound.SoundPackManager;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SoundPackSelectionScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SETTINGS_SLIDER_WIDTH = 150;
    private static final int SETTINGS_ROW_HEIGHT = 20;
    private static final int SETTINGS_START_Y = 34;
    private static final int SETTINGS_GAP_Y = 24;
    private static final int DONE_BUTTON_WIDTH = 308;
    private static final int HALF_BUTTON_WIDTH = 150;

    private final Screen parentScreen;
    private SoundPackList soundPackList;

    private List<String> initialActivePackIds;
    private List<String> currentWorkingActivePackIds;
    private MultiLineLabel noPacksLabel = MultiLineLabel.EMPTY;

    private float initialVolume;
    private int initialFadeInTicks;
    private int initialFadeOutTicks;
    private boolean initialShowNowPlayingHud;

    private float currentVolume;
    private int currentFadeInTicks;
    private int currentFadeOutTicks;
    private boolean currentShowNowPlayingHud;

    public SoundPackSelectionScreen(Screen parentScreen) {
        super(Component.translatable("gui.music_player.soundpack_selection.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null) {
            LOGGER.error("Minecraft instance is null during SoundPackSelectionScreen init. Aborting initialization.");
            return;
        }

        List<SoundPackInfo> availablePacks = Music_Player.soundPackManager.getLoadedSoundPacks();
        this.initialActivePackIds = new ArrayList<>(Music_Player.soundPackManager.getActiveSoundPackIds());
        this.currentWorkingActivePackIds = new ArrayList<>(this.initialActivePackIds);

        this.initialVolume = ClientPlaybackSettings.getMusicVolume();
        this.initialFadeInTicks = ClientPlaybackSettings.getFadeInTicks();
        this.initialFadeOutTicks = ClientPlaybackSettings.getFadeOutTicks();
        this.initialShowNowPlayingHud = ClientPlaybackSettings.shouldShowNowPlayingHud();

        this.currentVolume = this.initialVolume;
        this.currentFadeInTicks = this.initialFadeInTicks;
        this.currentFadeOutTicks = this.initialFadeOutTicks;
        this.currentShowNowPlayingHud = this.initialShowNowPlayingHud;

        int controlsLeft = this.width / 2 - 154;
        int controlsRight = this.width / 2 + 4;

        this.addRenderableWidget(new MusicVolumeSlider(controlsLeft, SETTINGS_START_Y, DONE_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT));
        this.addRenderableWidget(new FadeTicksSlider(
                controlsLeft,
                SETTINGS_START_Y + SETTINGS_GAP_Y,
                SETTINGS_SLIDER_WIDTH,
                SETTINGS_ROW_HEIGHT,
                true
        ));
        this.addRenderableWidget(new FadeTicksSlider(
                controlsRight,
                SETTINGS_START_Y + SETTINGS_GAP_Y,
                SETTINGS_SLIDER_WIDTH,
                SETTINGS_ROW_HEIGHT,
                false
        ));

        this.addRenderableWidget(Button.builder(buildNowPlayingToggleLabel(), button -> {
                    currentShowNowPlayingHud = !currentShowNowPlayingHud;
                    button.setMessage(buildNowPlayingToggleLabel());
                })
                .bounds(controlsLeft, SETTINGS_START_Y + SETTINGS_GAP_Y * 2, DONE_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.music_player.soundpack_selection.open_overlay_style"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new NowPlayingStyleScreen(this));
                    }
                })
                .bounds(controlsLeft, SETTINGS_START_Y + SETTINGS_GAP_Y * 3, DONE_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT)
                .build());

        int listTop = SETTINGS_START_Y + SETTINGS_GAP_Y * 3 + SETTINGS_ROW_HEIGHT + 6;
        int listBottom = this.height - 64;
        int listHeight = Math.max(0, listBottom - listTop);
        this.soundPackList = new SoundPackList(this.minecraft, this.width, listHeight, listTop, listBottom, 36, availablePacks, this);
        this.addWidget(this.soundPackList);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    applyChanges();
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(this.parentScreen);
                    } else {
                        LOGGER.warn("Minecraft instance was null when trying to close SoundPackSelectionScreen (Done button).");
                    }
                })
                .bounds(this.width / 2 - 154, this.height - 52, DONE_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.music_player.soundpack_selection.open_folder"), button -> Util.getPlatform().openUri(SoundPackManager.SOUNDPACKS_BASE_DIR.toUri()))
                .bounds(this.width / 2 - 154, this.height - 28, HALF_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.music_player.soundpack_selection.reload_packs"), button -> reloadPacks())
                .bounds(this.width / 2 + 4, this.height - 28, HALF_BUTTON_WIDTH, SETTINGS_ROW_HEIGHT)
                .build());

        if (availablePacks.isEmpty()) {
            this.noPacksLabel = MultiLineLabel.create(this.font, Component.translatable("gui.music_player.soundpack_selection.no_packs"), this.width - 50);
        } else {
            this.noPacksLabel = MultiLineLabel.EMPTY;
        }
    }

    private void reloadPacks() {
        Music_Player.soundPackManager.discoverAndLoadPacks();

        if (this.minecraft != null) {
            this.clearWidgets();
            this.init();
        } else {
            LOGGER.error("Minecraft instance is null. Cannot re-initialize screen after reloading packs.");
        }
    }

    private void applyChanges() {
        if (!this.initialActivePackIds.equals(this.currentWorkingActivePackIds)) {
            Music_Player.soundPackManager.setActiveSoundPackIds(this.currentWorkingActivePackIds);
            if (this.minecraft != null) {
                this.minecraft.reloadResourcePacks();
            }
            LOGGER.info("Applied sound pack changes and triggered resource reload.");
        } else {
            LOGGER.info("No changes in active sound packs to apply.");
        }

        boolean hasClientSettingsChanges = Float.compare(initialVolume, currentVolume) != 0
                || initialFadeInTicks != currentFadeInTicks
                || initialFadeOutTicks != currentFadeOutTicks
                || initialShowNowPlayingHud != currentShowNowPlayingHud;

        if (hasClientSettingsChanges) {
            ClientPlaybackSettings.setMusicVolume(currentVolume);
            ClientPlaybackSettings.setFadeInTicks(currentFadeInTicks);
            ClientPlaybackSettings.setFadeOutTicks(currentFadeOutTicks);
            ClientPlaybackSettings.setShowNowPlayingHud(currentShowNowPlayingHud);
            ClientPlaybackSettings.save();
            ClientMusicManager.onPlaybackSettingsChanged();
            LOGGER.info("Applied client playback settings. volume={}, fadeInTicks={}, fadeOutTicks={}, showNowPlayingHud={}",
                    currentVolume, currentFadeInTicks, currentFadeOutTicks, currentShowNowPlayingHud);
        }
    }

    public List<String> getCurrentWorkingActivePackIds() {
        return this.currentWorkingActivePackIds;
    }

    public void togglePackActivation(String packId) {
        if (this.currentWorkingActivePackIds.contains(packId)) {
            this.currentWorkingActivePackIds.remove(packId);
        } else {
            this.currentWorkingActivePackIds.add(packId);
        }
        if (this.soundPackList != null) {
            this.soundPackList.children().forEach(SoundPackList.Entry::updateSelectedStatus);
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.soundPackList != null) {
            this.soundPackList.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.noPacksLabel != MultiLineLabel.EMPTY) {
            this.noPacksLabel.renderCentered(guiGraphics, this.width / 2, this.height / 2 - this.noPacksLabel.getLineCount() * this.font.lineHeight / 2);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            if (this.parentScreen != null) {
                this.minecraft.setScreen(this.parentScreen);
            } else {
                LOGGER.warn("parentScreen is null in onClose. Cannot navigate back to parent.");
            }
        } else {
            LOGGER.warn("Minecraft instance was null during onClose of SoundPackSelectionScreen.");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (this.minecraft != null) {
                if (this.parentScreen != null) {
                    this.minecraft.setScreen(this.parentScreen);
                } else {
                    LOGGER.warn("parentScreen is null when trying to close via ESC key.");
                }
            } else {
                LOGGER.warn("Minecraft instance was null when trying to close SoundPackSelectionScreen via ESC key.");
            }
            return true;
        }
        return false;
    }

    private Component buildNowPlayingToggleLabel() {
        return Component.translatable(
                currentShowNowPlayingHud
                        ? "gui.music_player.soundpack_selection.now_playing.enabled"
                        : "gui.music_player.soundpack_selection.now_playing.disabled"
        );
    }

    private final class MusicVolumeSlider extends AbstractSliderButton {
        private MusicVolumeSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), currentVolume);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int percentage = Math.round((float) this.value * 100.0f);
            setMessage(Component.translatable("gui.music_player.soundpack_selection.music_volume", percentage));
        }

        @Override
        protected void applyValue() {
            currentVolume = (float) this.value;
        }
    }

    private final class FadeTicksSlider extends AbstractSliderButton {
        private static final int MAX_FADE_TICKS = 200;
        private final boolean isFadeIn;

        private FadeTicksSlider(int x, int y, int width, int height, boolean isFadeIn) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    (isFadeIn ? currentFadeInTicks : currentFadeOutTicks) / (double) MAX_FADE_TICKS
            );
            this.isFadeIn = isFadeIn;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int ticks = isFadeIn ? currentFadeInTicks : currentFadeOutTicks;
            double seconds = ticks / 20.0;
            String key = isFadeIn
                    ? "gui.music_player.soundpack_selection.fade_in"
                    : "gui.music_player.soundpack_selection.fade_out";
            setMessage(Component.translatable(key, String.format(Locale.ROOT, "%.1f", seconds)));
        }

        @Override
        protected void applyValue() {
            int ticks = (int) Math.round(this.value * MAX_FADE_TICKS);
            if (isFadeIn) {
                currentFadeInTicks = ticks;
            } else {
                currentFadeOutTicks = ticks;
            }
        }
    }
}
