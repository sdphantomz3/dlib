package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parent;
    private String activeSelectedMod = "";
    
    private double scrollAmount = 0;
    private final int rowHeight = 22;

    public ModListWidget(DLibMainScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int viewTop = this.getY() + 15;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;
        int totalHeight = ConfigManager.getRegisteredMods().size() * rowHeight;

        if (totalHeight <= viewHeight) return false;

        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
            int maxScroll = totalHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - scrollY * 14, maxScroll));
            return true;
        }
        return false;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // RenderUtil.drawScaledText(graphics, "Registered Mods", 1.2f, this.getX() + 14, this.getY() + 18, 0xFFFFFFFF, 1.0f, true);

        int viewTop = this.getY() + 15;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;
        List<String> mods = ConfigManager.getRegisteredMods();

        for (int i = 0; i < mods.size(); i++) {
            String modId = mods.get(i);
            int rowTopY = viewTop + (i * rowHeight) - (int)scrollAmount;

            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;

            boolean isHovered = mouseX >= this.getX() + 10 && mouseX < this.getX() + this.width - 15 
                    && mouseY >= rowTopY && mouseY < rowTopY + rowHeight;
            boolean isSelected = modId.equals(activeSelectedMod);

            if (isSelected) {
                graphics.fill(this.getX() + 10, rowTopY, this.getX() + this.width - 15, rowTopY + rowHeight - 2, 0x44FFFFFF);
            } else if (isHovered) {
                graphics.fill(this.getX() + 10, rowTopY, this.getX() + this.width - 15, rowTopY + rowHeight - 2, 0x11FFFFFF);
            }

            int textColor = isSelected ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : 0xFF999999);
            
            RenderUtil.drawScaledText(graphics,
            ConfigManager.getModDisplayName(modId),
            1.0f, this.getX() + 16, rowTopY + 5, textColor, 1.0f, true);
        }

        int totalHeight = mods.size() * rowHeight;
        if (totalHeight > viewHeight) {
            int scrollbarX = this.getX() + this.width - 8;
            int thumbHeight = Math.max(12, (viewHeight * viewHeight) / totalHeight);
            int maxScroll = totalHeight - viewHeight;
            int thumbY = viewTop + (int)((scrollAmount / maxScroll) * (viewHeight - thumbHeight));

            graphics.fill(scrollbarX, viewTop, scrollbarX + 3, viewBottom, 0x11FFFFFF);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbHeight, 0x66FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        double mx = event.x(); double my = event.y();
        int viewTop = this.getY() + 15;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;
        List<String> mods = ConfigManager.getRegisteredMods();
        int totalHeight = mods.size() * rowHeight;

        if (totalHeight > viewHeight && mx >= this.getX() + this.width - 12 && mx <= this.getX() + this.width && my >= viewTop && my <= viewBottom) {
            double clickPercentage = (my - viewTop) / (double)viewHeight;
            int maxScroll = totalHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(clickPercentage * maxScroll, maxScroll));
            return true;
        }

        for (int i = 0; i < mods.size(); i++) {
            int rowTopY = viewTop + (i * rowHeight) - (int)scrollAmount;
            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;

            if (mx >= this.getX() + 10 && mx < this.getX() + this.width - 15 && my >= rowTopY && my < rowTopY + rowHeight) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.activeSelectedMod = mods.get(i);
                this.parent.setSelectedMod(this.activeSelectedMod);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}