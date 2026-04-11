package com.zoma1101.music_player.client.overlay;

public enum UIAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE_CENTER,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public static UIAnchor fromString(String value, UIAnchor fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return UIAnchor.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
