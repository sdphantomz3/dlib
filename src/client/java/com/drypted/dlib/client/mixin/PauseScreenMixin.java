package com.drypted.dlib.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.drypted.dlib.DLib;
import com.drypted.dlib.client.gui.DLibMainScreen;
import com.drypted.dlib.client.util.RenderUtil;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void DLib$addConfigButton(CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        // Gear icon sprites
        WidgetSprites gearSprites = new WidgetSprites(
            Identifier.fromNamespaceAndPath(DLib.MOD_ID, "config"),
            Identifier.fromNamespaceAndPath(DLib.MOD_ID, "config-highlighted")
        );

        int iconSize = 20;
        int gap = 4;
        int margin = 10;

        String labelText = "Drypted Mod's Config";
        float scale = 1.0f;              // adjust as needed
        int color = 0xFFFFFF;            // white
        float alpha = 1.0f;
        boolean shadow = true;

        // Measure text (unscaled)
        int textWidth = this.minecraft.font.width(labelText);
        int totalWidth = iconSize + gap + (int)(textWidth * scale);

        // Bottom left
        int iconX = margin;
        int iconY = this.height - iconSize - margin;

        // Vertically centre text with the icon
        int textX = iconX + iconSize + gap;
        int textY = iconY + (int)((iconSize - this.minecraft.font.lineHeight * scale) / 2);

        // Create the clickable gear button
        ImageButton configButton = new ImageButton(
            iconX, iconY,
            iconSize, iconSize,
            gearSprites,
            button -> this.minecraft.setScreen(new DLibMainScreen(this))
        );

        // Add the button as a widget
        this.addRenderableWidget(configButton);

        // Add the label as a Renderable – no widget overhead
        this.addRenderableOnly((GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) -> {
            // If GuiGraphics is a wrapper, adapt accordingly.
            // For example, if it's a subclass, you might cast:
            GuiGraphics extractor = (GuiGraphics) guiGraphics;
            RenderUtil.drawScaledText(
                extractor,
                labelText,
                scale,
                textX,
                textY,
                color,
                alpha,
                shadow
            );
        });
    }
}