package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ConfigListWidget extends AbstractWidget {
    private String currentMod = null;
    private final Button testToggleButton;
    private boolean mockSetting = true;

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());

        this.testToggleButton = Button.builder(
            Component.literal("Example Setting: ON"),
            (btn) -> {
                this.mockSetting = !this.mockSetting;
                btn.setMessage(Component.literal("Example Setting: " + (this.mockSetting ? "ON" : "OFF")));
            }
        ).bounds(x + 20, y + 40, 150, 20).build();
    }

    public void setMod(String modName) {
        this.currentMod = modName;
    }

    // 1. Replaced renderWidget with extractWidgetRenderState
    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (this.currentMod == null) {
            // 2. Replaced drawCenteredString with centeredText
            graphics.centeredText(mc.font, "Select a mod from the left to configure.", 
                this.getX() + this.width / 2, this.getY() + this.height / 2, 0xFFAAAAAA);
            return;
        }

        // USING YOUR RENDER UTIL:
        RenderUtil.drawScaledText(
            graphics, 
            "Configuring: " + this.currentMod, 
            1.5f, 
            this.getX() + 20, 
            this.getY() + 15, 
            0xFFFFFF, 
            1.0f, 
            true
        );

        // 3. Replaced render() with extractRenderState()
        this.testToggleButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        // Pass the entire event down to the child button if a mod is selected
        if (this.currentMod != null && this.testToggleButton.mouseClicked(event, doubleClick)) {
            return true;
        }
        // Forward the event bundle to the super class
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Required, can leave empty
    }
}