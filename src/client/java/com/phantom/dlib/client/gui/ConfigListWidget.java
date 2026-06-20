package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.config.ConfigManager;
import com.phantom.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigListWidget extends AbstractWidget {
    private String currentMod = null;
    private final List<Button> activeOptionButtons = new ArrayList<>();

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * Triggered externally when a selection on the left side is selected.
     * Rebuilds button structures dynamically on demand.
     */
    public void setMod(String modName) {
        this.currentMod = modName;
        this.activeOptionButtons.clear();

        if (modName != null) {
            Map<String, Boolean> configOptions = ConfigManager.getOptionsForMod(modName);
            int idx = 0;

            for (String key : configOptions.keySet()) {
                boolean isEnabled = ConfigManager.getBoolean(modName, key);
                final String optionKey = key;

                // Create a real configuration toggle button
                Button configButton = Button.builder(
                    Component.literal(optionKey + ": " + (isEnabled ? "ON" : "OFF")),
                    (btn) -> {
                        // Reverse current file flag configuration value
                        boolean toggledState = !ConfigManager.getBoolean(this.currentMod, optionKey);
                        ConfigManager.setBoolean(this.currentMod, optionKey, toggledState);
                        
                        // Dynamically rename button label tracking values
                        btn.setMessage(Component.literal(optionKey + ": " + (toggledState ? "ON" : "OFF")));
                    }
                ).bounds(this.getX() + 20, this.getY() + 55 + (idx * 26), 220, 20).build();

                this.activeOptionButtons.add(configButton);
                idx++;
            }
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (this.currentMod == null) {
            graphics.centeredText(mc.font, "Select a mod from the left to configure.", 
                this.getX() + this.width / 2, this.getY() + this.height / 2, 0xFFAAAAAA);
            return;
        }

        // Draw the scaled Mod Title Header using your custom Render Engine
        RenderUtil.drawScaledText(
            graphics, 
            "Configuring: " + this.currentMod, 
            1.4f, 
            this.getX() + 20, 
            this.getY() + 18, 
            0xFFFFFF, 
            1.0f, 
            true
        );

        // Map state extraction triggers across all active toggle buttons
        for (Button button : activeOptionButtons) {
            button.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.currentMod != null) {
            // Forward mouse inputs straight down to children configuration toggles
            for (Button button : activeOptionButtons) {
                if (button.mouseClicked(event, doubleClick)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}