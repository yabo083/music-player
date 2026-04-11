package com.zoma1101.music_player.client.overlay;

public final class AnchorLayoutResolver {
    private AnchorLayoutResolver() {
    }

    public static Position resolveTopLeft(
            UIAnchor anchor,
            int screenWidth,
            int screenHeight,
            int boxWidth,
            int boxHeight,
            int offsetX,
            int offsetY
    ) {
        int x = switch (anchor) {
            case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> 0;
            case TOP_CENTER, MIDDLE_CENTER, BOTTOM_CENTER -> (screenWidth - boxWidth) / 2;
            case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> screenWidth - boxWidth;
        };

        int y = switch (anchor) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
            case MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT -> (screenHeight - boxHeight) / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - boxHeight;
        };

        return new Position(x + offsetX, y + offsetY);
    }

    public record Position(int x, int y) {
    }
}
