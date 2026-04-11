package com.zoma1101.music_player.client.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverlayAnimationResolverTest {

    @Test
    void cornersPreferHorizontalDirection() {
        assertEquals(OverlayAnimationType.SLIDE_LEFT, OverlayAnimationResolver.resolve(UIAnchor.TOP_LEFT));
        assertEquals(OverlayAnimationType.SLIDE_LEFT, OverlayAnimationResolver.resolve(UIAnchor.BOTTOM_LEFT));
        assertEquals(OverlayAnimationType.SLIDE_RIGHT, OverlayAnimationResolver.resolve(UIAnchor.TOP_RIGHT));
        assertEquals(OverlayAnimationType.SLIDE_RIGHT, OverlayAnimationResolver.resolve(UIAnchor.BOTTOM_RIGHT));
    }

    @Test
    void centerUsesFade() {
        assertEquals(OverlayAnimationType.FADE, OverlayAnimationResolver.resolve(UIAnchor.MIDDLE_CENTER));
    }

    @Test
    void topAndBottomCentersUseVerticalDirection() {
        assertEquals(OverlayAnimationType.SLIDE_UP, OverlayAnimationResolver.resolve(UIAnchor.TOP_CENTER));
        assertEquals(OverlayAnimationType.SLIDE_DOWN, OverlayAnimationResolver.resolve(UIAnchor.BOTTOM_CENTER));
    }
}
