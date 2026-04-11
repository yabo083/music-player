package com.zoma1101.music_player.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.Nullable;

public final class NowPlayingOverlayController implements MusicPlaybackObserver {
    @Nullable
    private OverlayToast toast;
    private long clientTick = 0L;
    private NowPlayingOverlayStyle style = NowPlayingOverlayStyle.createDefault();
    private final NowPlayingOverlayRenderer renderer = new NowPlayingOverlayRenderer();

    public void setStyle(NowPlayingOverlayStyle style) {
        this.style = style.copy();
        this.style.sanitize();
        if (!this.style.isEnabled()) {
            toast = null;
        }
    }

    public NowPlayingOverlayStyle getStyleCopy() {
        return style.copy();
    }

    public void onClientTick() {
        clientTick++;
        if (toast == null) {
            return;
        }
        if (isCompletelyFinished(clientTick, toast, style)) {
            toast = null;
        }
    }

    @Override
    public void onTrackStarted(String trackDisplayName) {
        if (!style.isEnabled()) {
            toast = null;
            return;
        }
        if (trackDisplayName == null || trackDisplayName.isBlank()) {
            return;
        }
        toast = new OverlayToast(trackDisplayName, clientTick);
    }

    @Override
    public void onTrackStopped() {
        if (toast == null) {
            return;
        }
        toast = toast.triggerExit(clientTick, style.getEnterDurationTicks(), style.getDisplayDurationTicks());
    }

    public void render(RenderGuiEvent.Post event) {
        if (!style.isEnabled() || toast == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        Font font = mc.font;
        String text = toast.text();
        int textWidth = renderer.measureWidth(font, text);
        int textHeight = renderer.measureHeight(font);

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        AnchorLayoutResolver.Position target = AnchorLayoutResolver.resolveTopLeft(
                style.getAnchor(),
                screenWidth,
                screenHeight,
                textWidth,
                textHeight,
                style.getOffsetX(),
                style.getOffsetY()
        );

        AnimationFrame frame = computeFrame(clientTick + event.getPartialTick(), toast, style);
        if (frame == null) {
            return;
        }

        int finalX = Math.round(target.x() + frame.offsetX());
        int finalY = Math.round(target.y() + frame.offsetY());
        GuiGraphics guiGraphics = event.getGuiGraphics();
        renderer.render(guiGraphics, font, text, finalX, finalY, frame.alpha(), style);
    }

    @Nullable
    private static AnimationFrame computeFrame(double nowTick, OverlayToast toast, NowPlayingOverlayStyle style) {
        int enterTicks = style.getEnterDurationTicks();
        int holdTicks = style.getDisplayDurationTicks();
        int exitTicks = style.getExitDurationTicks();
        long startTick = toast.startTick();

        double enterEnd = startTick + enterTicks;
        double defaultExitStart = enterEnd + holdTicks;
        double exitStart = toast.hasForcedExit()
                ? Math.min(defaultExitStart, toast.forcedExitTick())
                : defaultExitStart;

        if (enterTicks > 0 && nowTick < enterEnd) {
            float p = (float) ((nowTick - startTick) / enterTicks);
            return enteringFrame(style, ease(p));
        }

        if (nowTick < exitStart) {
            return new AnimationFrame(0.0f, 0.0f, 1.0f);
        }

        if (exitTicks <= 0) {
            return null;
        }

        double exitEnd = exitStart + exitTicks;
        if (nowTick >= exitEnd) {
            return null;
        }

        float p = (float) ((nowTick - exitStart) / exitTicks);
        return exitingFrame(style, ease(p));
    }

    private static boolean isCompletelyFinished(long nowTick, OverlayToast toast, NowPlayingOverlayStyle style) {
        int enterTicks = style.getEnterDurationTicks();
        int holdTicks = style.getDisplayDurationTicks();
        int exitTicks = style.getExitDurationTicks();

        long startTick = toast.startTick();
        long defaultExitStart = startTick + enterTicks + holdTicks;
        long exitStart = toast.hasForcedExit()
                ? Math.min(defaultExitStart, toast.forcedExitTick())
                : defaultExitStart;

        return exitTicks <= 0 ? nowTick >= exitStart : nowTick >= exitStart + exitTicks;
    }

    private static AnimationFrame enteringFrame(NowPlayingOverlayStyle style, float progress) {
        float offsetScale = 1.0f - progress;
        return createDirectionalFrame(style, offsetScale, progress, false);
    }

    private static AnimationFrame exitingFrame(NowPlayingOverlayStyle style, float progress) {
        return createDirectionalFrame(style, progress, 1.0f - progress, true);
    }

    private static AnimationFrame createDirectionalFrame(NowPlayingOverlayStyle style, float offsetScale, float alpha, boolean exiting) {
        OverlayAnimationType animation = OverlayAnimationResolver.resolve(style.getAnchor());
        float distance = style.getAnimationDistancePx() * offsetScale;

        float dx = 0.0f;
        float dy = 0.0f;

        switch (animation) {
            case SLIDE_LEFT -> dx = -distance;
            case SLIDE_RIGHT -> dx = distance;
            case SLIDE_UP -> dy = -distance;
            case SLIDE_DOWN -> dy = distance;
            case FADE -> {
                dx = 0.0f;
                dy = 0.0f;
            }
        }

        if (!exiting) {
            return new AnimationFrame(dx, dy, alpha);
        }
        return new AnimationFrame(dx, dy, alpha);
    }

    private static float ease(float progress) {
        float clamped = Mth.clamp(progress, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    private record OverlayToast(String text, long startTick, boolean hasForcedExit, long forcedExitTick) {
        private OverlayToast(String text, long startTick) {
            this(text, startTick, false, 0L);
        }

        private OverlayToast triggerExit(long nowTick, int enterTicks, int holdTicks) {
            long naturalExit = startTick + enterTicks + holdTicks;
            long forced = Math.min(naturalExit, nowTick);
            return new OverlayToast(text, startTick, true, forced);
        }
    }

    private record AnimationFrame(float offsetX, float offsetY, float alpha) {
    }
}
