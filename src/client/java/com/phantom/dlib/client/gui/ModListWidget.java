package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parentScreen;
    private final List<String> registeredMods;
    private int selectedIndex = -1;

    public ModListWidget(DLibMainScreen parentScreen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parentScreen = parentScreen;
        this.registeredMods = ConfigManager.getRegisteredMods();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x55000000);

        int itemHeight = 24;
        for (int i = 0; i < registeredMods.size(); i++) {
            int itemY = this.getY() + (i * itemHeight) + 8;
            int itemWidth = this.width - 8;
            
            boolean isHovered = mouseX >= this.getX() + 4 && mouseX <= this.getX() + 4 + itemWidth 
                    && mouseY >= itemY && mouseY < itemY + itemHeight - 2;
            
            if (i == selectedIndex) {
                graphics.fill(this.getX() + 4, itemY, this.getX() + 4 + itemWidth, itemY + itemHeight - 2, 0x44FFFFFF);
            } else if (isHovered) {
                graphics.fill(this.getX() + 4, itemY, this.getX() + 4 + itemWidth, itemY + itemHeight - 2, 0x22FFFFFF);
            }

            int borderColor = (i == selectedIndex) ? 0xFFFFFFFF : 0xFF555555; 
            graphics.outline(this.getX() + 4, itemY, itemWidth, itemHeight - 2, borderColor);

            graphics.text(mc.font, registeredMods.get(i), this.getX() + 12, itemY + 6, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (!this.isActive()) return false;

        double mouseX = event.x();
        double mouseY = event.y();

        if (mouseX >= this.getX() + 4 && mouseX <= this.getX() + this.width - 4) {
            int itemHeight = 24;
            for (int i = 0; i < registeredMods.size(); i++) {
                int itemY = this.getY() + (i * itemHeight) + 8;
                if (mouseY >= itemY && mouseY < itemY + itemHeight - 2) {
                    this.selectedIndex = i;
                    this.parentScreen.setSelectedMod(registeredMods.get(i));
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}