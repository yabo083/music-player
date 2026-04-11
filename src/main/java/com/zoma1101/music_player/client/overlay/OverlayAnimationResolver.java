package com.zoma1101.music_player.client.overlay;

public final class OverlayAnimationResolver {
    private OverlayAnimationResolver() {
    }

    public static OverlayAnimationType resolve(UIAnchor anchor) {
        return switch (anchor) {
            case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> OverlayAnimationType.SLIDE_LEFT;
            case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> OverlayAnimationType.SLIDE_RIGHT;
            case TOP_CENTER -> OverlayAnimationType.SLIDE_UP;
            case BOTTOM_CENTER -> OverlayAnimationType.SLIDE_DOWN;
            case MIDDLE_CENTER -> OverlayAnimationType.FADE;
        };
    }
}
