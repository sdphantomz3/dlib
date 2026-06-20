package com.phantom.dlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parentScreen;
    private final List<String> mockMods = List.of("PvpEssentials", "PhantomCore", "DLib-Engine", "ExampleMod");
    private int selectedIndex = -1;

    public ModListWidget(DLibMainScreen parentScreen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parentScreen = parentScreen;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Draw a dark translucent backdrop for the left panel sidebar
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x33000000);

        // 2. Iterate and draw list entries
        int itemHeight = 24;
        for (int i = 0; i < mockMods.size(); i++) {
            int itemY = this.getY() + (i * itemHeight) + 5;
            boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.width 
                    && mouseY >= itemY && mouseY < itemY + itemHeight;
            
            // Highlight active selections and cursor hovers
            if (i == selectedIndex) {
                graphics.fill(this.getX() + 4, itemY, this.getX() + this.width - 4, itemY + itemHeight - 2, 0x66FFFFFF);
            } else if (isHovered) {
                graphics.fill(this.getX() + 4, itemY, this.getX() + this.width - 4, itemY + itemHeight - 2, 0x33FFFFFF);
            }

            // Render text string using the non-deprecated graphics extractor engine
            graphics.text(mc.font, mockMods.get(i), this.getX() + 10, itemY + 6, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (!this.isActive()) return false;

        double mouseX = event.x();
        double mouseY = event.y();

        // Route clicks specifically into the list rows
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width) {
            int itemHeight = 24;
            for (int i = 0; i < mockMods.size(); i++) {
                int itemY = this.getY() + (i * itemHeight) + 5;
                if (mouseY >= itemY && mouseY < itemY + itemHeight) {
                    this.selectedIndex = i;
                    this.parentScreen.setSelectedMod(mockMods.get(i));
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
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