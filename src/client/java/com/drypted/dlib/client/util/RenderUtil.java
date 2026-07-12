package com.drypted.dlib.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

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

    /**
     * Draws a sprite from the texture atlas using the default GUI render pipeline.
     *
     * @param g      GuiGraphicsExtractor context
     * @param sprite Identifier of the sprite
     * @param x      X position
     * @param y      Y position
     * @param width  Width in pixels
     * @param height Height in pixels
     */
    public static void drawSprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int width, int height) {
        drawSprite(g, RenderPipelines.GUI, sprite, x, y, width, height);
    }

    /**
     * Draws a sprite from the texture atlas with a specified render pipeline.
     *
     * @param g        GuiGraphicsExtractor context
     * @param pipeline RenderPipeline to use (e.g. RenderPipelines.GUI)
     * @param sprite   Identifier of the sprite
     * @param x        X position
     * @param y        Y position
     * @param width    Width in pixels
     * @param height   Height in pixels
     */
    public static void drawSprite(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        g.blitSprite(pipeline, sprite, x, y, width, height);
    }

    /**
     * Draws an item icon using the full 3D item model rendering.
     *
     * @param g      GuiGraphicsExtractor context
     * @param stack  The ItemStack to render
     * @param x      X position
     * @param y      Y position
     * @param width  Width in pixels (ignored; item renders at native size)
     * @param height Height in pixels (ignored; item renders at native size)
     */
    public static void drawItemIcon(GuiGraphicsExtractor g, ItemStack stack, int x, int y, int width, int height) {
        if (stack == null || stack.isEmpty()) return;
        try {
            g.fakeItem(stack, x, y);
            g.itemDecorations(Minecraft.getInstance().font, stack, x, y, null);
        } catch (Exception ignored) {
            // Draw a fallback purple placeholder
            g.fill(x, y, x + width, y + height, 0xFF8844AA);
        }
    }
}