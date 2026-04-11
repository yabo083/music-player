package com.zoma1101.music_player.client.overlay;

import java.util.Objects;

public final class NowPlayingOverlayStyle {
    public static final UIAnchor DEFAULT_ANCHOR = UIAnchor.BOTTOM_CENTER;
    public static final int DEFAULT_OFFSET_X = 0;
    public static final int DEFAULT_OFFSET_Y = -68;
    public static final int DEFAULT_TEXT_COLOR_RGBA = 0xFFFF55FF;
    public static final int DEFAULT_BACKGROUND_COLOR_RGBA = 0x66000000;
    public static final int DEFAULT_DISPLAY_DURATION_TICKS = 60;
    public static final int DEFAULT_ENTER_DURATION_TICKS = 8;
    public static final int DEFAULT_EXIT_DURATION_TICKS = 8;
    public static final int DEFAULT_ANIMATION_DISTANCE_PX = 18;

    private boolean enabled = true;
    private UIAnchor anchor = DEFAULT_ANCHOR;
    private int offsetX = DEFAULT_OFFSET_X;
    private int offsetY = DEFAULT_OFFSET_Y;
    private int textColorRgba = DEFAULT_TEXT_COLOR_RGBA;
    private boolean showBackground = false;
    private int backgroundColorRgba = DEFAULT_BACKGROUND_COLOR_RGBA;
    private int displayDurationTicks = DEFAULT_DISPLAY_DURATION_TICKS;
    private int enterDurationTicks = DEFAULT_ENTER_DURATION_TICKS;
    private int exitDurationTicks = DEFAULT_EXIT_DURATION_TICKS;
    private int animationDistancePx = DEFAULT_ANIMATION_DISTANCE_PX;
    private boolean textShadow = true;

    public static NowPlayingOverlayStyle createDefault() {
        return new NowPlayingOverlayStyle();
    }

    public NowPlayingOverlayStyle copy() {
        NowPlayingOverlayStyle style = new NowPlayingOverlayStyle();
        style.enabled = enabled;
        style.anchor = anchor;
        style.offsetX = offsetX;
        style.offsetY = offsetY;
        style.textColorRgba = textColorRgba;
        style.showBackground = showBackground;
        style.backgroundColorRgba = backgroundColorRgba;
        style.displayDurationTicks = displayDurationTicks;
        style.enterDurationTicks = enterDurationTicks;
        style.exitDurationTicks = exitDurationTicks;
        style.animationDistancePx = animationDistancePx;
        style.textShadow = textShadow;
        return style;
    }

    public void sanitize() {
        anchor = Objects.requireNonNullElse(anchor, DEFAULT_ANCHOR);
        textColorRgba = RgbaColor.sanitize(textColorRgba);
        backgroundColorRgba = RgbaColor.sanitize(backgroundColorRgba);
        displayDurationTicks = Math.max(0, displayDurationTicks);
        enterDurationTicks = Math.max(0, enterDurationTicks);
        exitDurationTicks = Math.max(0, exitDurationTicks);
        animationDistancePx = Math.max(0, animationDistancePx);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UIAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(UIAnchor anchor) {
        this.anchor = anchor;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getTextColorRgba() {
        return textColorRgba;
    }

    public void setTextColorRgba(int textColorRgba) {
        this.textColorRgba = textColorRgba;
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
    }

    public int getBackgroundColorRgba() {
        return backgroundColorRgba;
    }

    public void setBackgroundColorRgba(int backgroundColorRgba) {
        this.backgroundColorRgba = backgroundColorRgba;
    }

    public int getDisplayDurationTicks() {
        return displayDurationTicks;
    }

    public void setDisplayDurationTicks(int displayDurationTicks) {
        this.displayDurationTicks = displayDurationTicks;
    }

    public int getEnterDurationTicks() {
        return enterDurationTicks;
    }

    public void setEnterDurationTicks(int enterDurationTicks) {
        this.enterDurationTicks = enterDurationTicks;
    }

    public int getExitDurationTicks() {
        return exitDurationTicks;
    }

    public void setExitDurationTicks(int exitDurationTicks) {
        this.exitDurationTicks = exitDurationTicks;
    }

    public int getAnimationDistancePx() {
        return animationDistancePx;
    }

    public void setAnimationDistancePx(int animationDistancePx) {
        this.animationDistancePx = animationDistancePx;
    }

    public boolean isTextShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }
}
