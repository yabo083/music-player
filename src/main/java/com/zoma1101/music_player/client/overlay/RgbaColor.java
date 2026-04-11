package com.zoma1101.music_player.client.overlay;

public final class RgbaColor {
    private RgbaColor() {
    }

    public static int sanitize(int argb) {
        return argb;
    }

    public static int withMultipliedAlpha(int argb, float alphaFactor) {
        float clamped = Math.max(0.0f, Math.min(1.0f, alphaFactor));
        int alpha = (argb >>> 24) & 0xFF;
        int multiplied = Math.max(0, Math.min(255, Math.round(alpha * clamped)));
        return (multiplied << 24) | (argb & 0x00FFFFFF);
    }

    public static int fromChannels(int r, int g, int b, int a) {
        return ((clamp(a) & 0xFF) << 24)
                | ((clamp(r) & 0xFF) << 16)
                | ((clamp(g) & 0xFF) << 8)
                | (clamp(b) & 0xFF);
    }

    public static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
