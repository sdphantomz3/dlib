package com.phantom.dlib.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class RenderUtil {

    /**
     * Draws text that scales accurately and calculates precise alpha shifts.
     */
    public static void drawScaledText(GuiGraphicsExtractor g, String text, float scale, int x, int y, int colorInt, float alpha, boolean drawShadow) {
        int r = (colorInt >> 16) & 0xFF;
        int gChan = (colorInt >> 8) & 0xFF;
        int b = colorInt & 0xFF;
        int a = (int) (((colorInt >> 24) & 0xFF) * alpha);
        
        // Default to maximum alpha base if none is embedded inside the hex color
        if (((colorInt >> 24) & 0xFF) == 0) {
            a = (int) (255 * alpha);
        }
        
        int finalColor = (a << 24) | (r << 16) | (gChan << 8) | b;

        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.text(Minecraft.getInstance().font, text, 0, 0, finalColor, drawShadow);
        g.pose().popMatrix();
    }
}