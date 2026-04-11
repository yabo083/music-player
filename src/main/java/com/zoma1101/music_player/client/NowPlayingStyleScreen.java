package com.zoma1101.music_player.client;

import com.zoma1101.music_player.ClientMusicManager;
import com.zoma1101.music_player.client.overlay.AnchorLayoutResolver;
import com.zoma1101.music_player.client.overlay.NowPlayingOverlayRenderer;
import com.zoma1101.music_player.client.overlay.NowPlayingOverlayStyle;
import com.zoma1101.music_player.client.overlay.OverlayAnimationResolver;
import com.zoma1101.music_player.client.overlay.OverlayAnimationType;
import com.zoma1101.music_player.client.overlay.RgbaColor;
import com.zoma1101.music_player.client.overlay.UIAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class NowPlayingStyleScreen extends Screen {
    private static final int PANEL_WIDTH = 150;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 24;
    private static final String PREVIEW_TEXT = "Now Playing: C418 - strad";

    private final Screen parentScreen;
    private final NowPlayingOverlayRenderer previewRenderer = new NowPlayingOverlayRenderer();
    private final NowPlayingOverlayStyle initialStyle;
    private final NowPlayingOverlayStyle workingStyle;
    private boolean committed = false;
    private int previewTick = 0;

    private Button anchorButton;
    private Button backgroundToggleButton;
    private Button textShadowButton;

    public NowPlayingStyleScreen(Screen parentScreen) {
        super(Component.translatable("gui.music_player.overlay_style.title"));
        this.parentScreen = parentScreen;
        this.initialStyle = ClientPlaybackSettings.getOverlayStyle();
        this.workingStyle = this.initialStyle.copy();
    }

    @Override
    protected void init() {
        super.init();

        int leftX = this.width / 2 - 154;
        int rightX = this.width / 2 + 4;
        int row = 0;

        this.anchorButton = addRenderableWidget(Button.builder(anchorLabel(), button -> {
                    workingStyle.setAnchor(nextAnchor(workingStyle.getAnchor()));
                    button.setMessage(anchorLabel());
                    applyLivePreview();
                })
                .bounds(leftX, rowY(row++), 308, ROW_HEIGHT)
                .build());

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, -300, 300,
                workingStyle::getOffsetX, workingStyle::setOffsetX, "gui.music_player.overlay_style.offset_x"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, -300, 300,
                workingStyle::getOffsetY, workingStyle::setOffsetY, "gui.music_player.overlay_style.offset_y"));

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 120,
                workingStyle::getEnterDurationTicks, workingStyle::setEnterDurationTicks, "gui.music_player.overlay_style.enter_ticks"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 240,
                workingStyle::getDisplayDurationTicks, workingStyle::setDisplayDurationTicks, "gui.music_player.overlay_style.display_ticks"));

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 120,
                workingStyle::getExitDurationTicks, workingStyle::setExitDurationTicks, "gui.music_player.overlay_style.exit_ticks"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 80,
                workingStyle::getAnimationDistancePx, workingStyle::setAnimationDistancePx, "gui.music_player.overlay_style.animation_distance"));

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.red(workingStyle.getTextColorRgba()),
                value -> setTextColorChannel(Channel.RED, value),
                "gui.music_player.overlay_style.text_r"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.green(workingStyle.getTextColorRgba()),
                value -> setTextColorChannel(Channel.GREEN, value),
                "gui.music_player.overlay_style.text_g"));

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.blue(workingStyle.getTextColorRgba()),
                value -> setTextColorChannel(Channel.BLUE, value),
                "gui.music_player.overlay_style.text_b"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.alpha(workingStyle.getTextColorRgba()),
                value -> setTextColorChannel(Channel.ALPHA, value),
                "gui.music_player.overlay_style.text_a"));

        this.backgroundToggleButton = addRenderableWidget(Button.builder(backgroundToggleLabel(), button -> {
                    workingStyle.setShowBackground(!workingStyle.isShowBackground());
                    button.setMessage(backgroundToggleLabel());
                    applyLivePreview();
                })
                .bounds(leftX, rowY(row++), 308, ROW_HEIGHT)
                .build());

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.red(workingStyle.getBackgroundColorRgba()),
                value -> setBackgroundColorChannel(Channel.RED, value),
                "gui.music_player.overlay_style.bg_r"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.green(workingStyle.getBackgroundColorRgba()),
                value -> setBackgroundColorChannel(Channel.GREEN, value),
                "gui.music_player.overlay_style.bg_g"));

        addRenderableWidget(new IntSlider(leftX, rowY(row), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.blue(workingStyle.getBackgroundColorRgba()),
                value -> setBackgroundColorChannel(Channel.BLUE, value),
                "gui.music_player.overlay_style.bg_b"));
        addRenderableWidget(new IntSlider(rightX, rowY(row++), PANEL_WIDTH, ROW_HEIGHT, 0, 255,
                () -> RgbaColor.alpha(workingStyle.getBackgroundColorRgba()),
                value -> setBackgroundColorChannel(Channel.ALPHA, value),
                "gui.music_player.overlay_style.bg_a"));

        this.textShadowButton = addRenderableWidget(Button.builder(textShadowLabel(), button -> {
                    workingStyle.setTextShadow(!workingStyle.isTextShadow());
                    button.setMessage(textShadowLabel());
                    applyLivePreview();
                })
                .bounds(leftX, rowY(row++), 308, ROW_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.music_player.overlay_style.reset_defaults"), button -> {
                    resetToDefaults();
                    button.setFocused(false);
                })
                .bounds(leftX, this.height - 76, PANEL_WIDTH, ROW_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    commitAndClose();
                })
                .bounds(rightX, this.height - 76, PANEL_WIDTH, ROW_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
                    closeWithoutSaving();
                })
                .bounds(leftX, this.height - 52, 308, ROW_HEIGHT)
                .build());

        applyLivePreview();
    }

    @Override
    public void tick() {
        super.tick();
        previewTick++;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.music_player.overlay_style.preview_hint"),
                this.width / 2,
                this.height - 96,
                0xA0A0A0
        );

        renderPreviewOverlay(guiGraphics, partialTicks);
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            closeWithoutSaving();
            return true;
        }
        return false;
    }

    private void renderPreviewOverlay(GuiGraphics guiGraphics, float partialTicks) {
        PreviewFrame frame = computePreviewFrame(previewTick + partialTicks);
        if (frame == null) {
            return;
        }

        int boxWidth = previewRenderer.measureWidth(this.font, PREVIEW_TEXT);
        int boxHeight = previewRenderer.measureHeight(this.font);
        AnchorLayoutResolver.Position base = AnchorLayoutResolver.resolveTopLeft(
                workingStyle.getAnchor(),
                this.width,
                this.height,
                boxWidth,
                boxHeight,
                workingStyle.getOffsetX(),
                workingStyle.getOffsetY()
        );

        int x = Math.round(base.x() + frame.offsetX());
        int y = Math.round(base.y() + frame.offsetY());
        previewRenderer.render(guiGraphics, this.font, PREVIEW_TEXT, x, y, frame.alpha(), workingStyle);
    }

    private PreviewFrame computePreviewFrame(float nowTick) {
        int enter = Math.max(0, workingStyle.getEnterDurationTicks());
        int hold = Math.max(0, workingStyle.getDisplayDurationTicks());
        int exit = Math.max(0, workingStyle.getExitDurationTicks());
        int pause = 20;
        int cycle = Math.max(1, enter + hold + exit + pause);
        float local = nowTick % cycle;

        if (enter > 0 && local < enter) {
            float p = ease(local / enter);
            return buildFrame(1.0f - p, p);
        }

        local -= enter;
        if (local < hold) {
            return new PreviewFrame(0.0f, 0.0f, 1.0f);
        }

        local -= hold;
        if (exit > 0 && local < exit) {
            float p = ease(local / exit);
            return buildFrame(p, 1.0f - p);
        }

        return null;
    }

    private PreviewFrame buildFrame(float offsetScale, float alpha) {
        OverlayAnimationType type = OverlayAnimationResolver.resolve(workingStyle.getAnchor());
        float distance = workingStyle.getAnimationDistancePx() * offsetScale;
        float dx = 0.0f;
        float dy = 0.0f;

        switch (type) {
            case SLIDE_LEFT -> dx = -distance;
            case SLIDE_RIGHT -> dx = distance;
            case SLIDE_UP -> dy = -distance;
            case SLIDE_DOWN -> dy = distance;
            case FADE -> {
                dx = 0.0f;
                dy = 0.0f;
            }
        }

        return new PreviewFrame(dx, dy, Mth.clamp(alpha, 0.0f, 1.0f));
    }

    private static float ease(float progress) {
        float clamped = Mth.clamp(progress, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    private void applyLivePreview() {
        workingStyle.sanitize();
        ClientPlaybackSettings.setOverlayStyle(workingStyle);
        ClientMusicManager.onPlaybackSettingsChanged();
        if (anchorButton != null) {
            anchorButton.setMessage(anchorLabel());
        }
        if (backgroundToggleButton != null) {
            backgroundToggleButton.setMessage(backgroundToggleLabel());
        }
        if (textShadowButton != null) {
            textShadowButton.setMessage(textShadowLabel());
        }
    }

    private void commitAndClose() {
        committed = true;
        ClientPlaybackSettings.setOverlayStyle(workingStyle);
        ClientPlaybackSettings.save();
        ClientMusicManager.onPlaybackSettingsChanged();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    private void closeWithoutSaving() {
        if (!committed) {
            ClientPlaybackSettings.setOverlayStyle(initialStyle);
            ClientMusicManager.onPlaybackSettingsChanged();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    private void resetToDefaults() {
        NowPlayingOverlayStyle defaults = NowPlayingOverlayStyle.createDefault();
        copyStyle(defaults, workingStyle);
        applyLivePreview();
    }

    private static void copyStyle(NowPlayingOverlayStyle from, NowPlayingOverlayStyle to) {
        to.setEnabled(from.isEnabled());
        to.setAnchor(from.getAnchor());
        to.setOffsetX(from.getOffsetX());
        to.setOffsetY(from.getOffsetY());
        to.setTextColorRgba(from.getTextColorRgba());
        to.setShowBackground(from.isShowBackground());
        to.setBackgroundColorRgba(from.getBackgroundColorRgba());
        to.setDisplayDurationTicks(from.getDisplayDurationTicks());
        to.setEnterDurationTicks(from.getEnterDurationTicks());
        to.setExitDurationTicks(from.getExitDurationTicks());
        to.setAnimationDistancePx(from.getAnimationDistancePx());
        to.setTextShadow(from.isTextShadow());
    }

    private Component anchorLabel() {
        return Component.translatable(
                "gui.music_player.overlay_style.anchor",
                Component.translatable("gui.music_player.overlay_style.anchor." + workingStyle.getAnchor().name().toLowerCase(Locale.ROOT))
        );
    }

    private Component backgroundToggleLabel() {
        return Component.translatable(
                workingStyle.isShowBackground()
                        ? "gui.music_player.overlay_style.background_enabled"
                        : "gui.music_player.overlay_style.background_disabled"
        );
    }

    private Component textShadowLabel() {
        return Component.translatable(
                workingStyle.isTextShadow()
                        ? "gui.music_player.overlay_style.text_shadow_enabled"
                        : "gui.music_player.overlay_style.text_shadow_disabled"
        );
    }

    private static UIAnchor nextAnchor(UIAnchor current) {
        UIAnchor[] values = UIAnchor.values();
        int index = current.ordinal();
        return values[(index + 1) % values.length];
    }

    private int rowY(int row) {
        return 38 + row * ROW_GAP;
    }

    private void setTextColorChannel(Channel channel, int value) {
        workingStyle.setTextColorRgba(updateChannel(workingStyle.getTextColorRgba(), channel, value));
        applyLivePreview();
    }

    private void setBackgroundColorChannel(Channel channel, int value) {
        workingStyle.setBackgroundColorRgba(updateChannel(workingStyle.getBackgroundColorRgba(), channel, value));
        applyLivePreview();
    }

    private static int updateChannel(int argb, Channel channel, int value) {
        int r = RgbaColor.red(argb);
        int g = RgbaColor.green(argb);
        int b = RgbaColor.blue(argb);
        int a = RgbaColor.alpha(argb);
        int clamped = Math.max(0, Math.min(255, value));

        switch (channel) {
            case RED -> r = clamped;
            case GREEN -> g = clamped;
            case BLUE -> b = clamped;
            case ALPHA -> a = clamped;
        }
        return RgbaColor.fromChannels(r, g, b, a);
    }

    private enum Channel {
        RED,
        GREEN,
        BLUE,
        ALPHA
    }

    private final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntSupplier getter;
        private final IntConsumer setter;
        private final String translationKey;

        private IntSlider(
                int x,
                int y,
                int width,
                int height,
                int min,
                int max,
                IntSupplier getter,
                IntConsumer setter,
                String translationKey
        ) {
            super(x, y, width, height, Component.empty(), toSliderValue(getter.getAsInt(), min, max));
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            this.translationKey = translationKey;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey, getter.getAsInt()));
        }

        @Override
        protected void applyValue() {
            int value = fromSliderValue(this.value, min, max);
            setter.accept(value);
        }
    }

    private static double toSliderValue(int current, int min, int max) {
        if (max <= min) {
            return 0.0;
        }
        return (double) (current - min) / (double) (max - min);
    }

    private static int fromSliderValue(double value, int min, int max) {
        if (max <= min) {
            return min;
        }
        double clamped = Mth.clamp(value, 0.0D, 1.0D);
        return min + (int) Math.round(clamped * (max - min));
    }

    private record PreviewFrame(float offsetX, float offsetY, float alpha) {
    }
}
