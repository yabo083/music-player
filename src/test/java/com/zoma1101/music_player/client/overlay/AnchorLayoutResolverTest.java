package com.zoma1101.music_player.client.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnchorLayoutResolverTest {

    @Test
    void resolvesBottomCenterWithOffset() {
        AnchorLayoutResolver.Position position = AnchorLayoutResolver.resolveTopLeft(
                UIAnchor.BOTTOM_CENTER,
                1920,
                1080,
                200,
                20,
                10,
                -30
        );

        assertEquals(870, position.x());
        assertEquals(1030, position.y());
    }

    @Test
    void resolvesMiddleRightWithOffset() {
        AnchorLayoutResolver.Position position = AnchorLayoutResolver.resolveTopLeft(
                UIAnchor.MIDDLE_RIGHT,
                1280,
                720,
                180,
                18,
                -8,
                12
        );

        assertEquals(1092, position.x());
        assertEquals(363, position.y());
    }

    @Test
    void resolvesTopLeftWithoutOffset() {
        AnchorLayoutResolver.Position position = AnchorLayoutResolver.resolveTopLeft(
                UIAnchor.TOP_LEFT,
                854,
                480,
                100,
                16,
                0,
                0
        );

        assertEquals(0, position.x());
        assertEquals(0, position.y());
    }
}
