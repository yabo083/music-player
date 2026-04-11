package com.zoma1101.music_player.client.overlay;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class NowPlayingOverlayRenderer {
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 3;

    public void render(
            GuiGraphics guiGraphics,
            Font font,
            String text,
            int left,
            int top,
            float alpha,
            NowPlayingOverlayStyle style
    ) {
        int textWidth = font.width(text);
        int boxWidth = textWidth + PADDING_X * 2;
        int boxHeight = font.lineHeight + PADDING_Y * 2;
        int right = left + boxWidth;
        int bottom = top + boxHeight;

        if (style.isShowBackground()) {
            int bgColor = RgbaColor.withMultipliedAlpha(style.getBackgroundColorRgba(), alpha);
            guiGraphics.fill(left, top, right, bottom, bgColor);
        }

        int textColor = RgbaColor.withMultipliedAlpha(style.getTextColorRgba(), alpha);
        guiGraphics.drawString(font, text, left + PADDING_X, top + PADDING_Y, textColor, style.isTextShadow());
    }

    public int measureWidth(Font font, String text) {
        return font.width(text) + PADDING_X * 2;
    }

    public int measureHeight(Font font) {
        return font.lineHeight + PADDING_Y * 2;
    }
}
