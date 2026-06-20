package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DLibMainScreen extends Screen {
    private final Screen parentScreen;
    private ModListWidget modListWidget;
    private ConfigListWidget configListWidget;

    public DLibMainScreen(Screen parentScreen) {
        super(Component.literal("DLib Config Manager"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        // Ensure data states are perfectly synced from files upon opening GUI
        ConfigManager.load();

        int leftPanelWidth = 140;

        this.modListWidget = new ModListWidget(this, 0, 0, leftPanelWidth, this.height);
        this.addRenderableWidget(this.modListWidget);

        this.configListWidget = new ConfigListWidget(leftPanelWidth, 0, this.width - leftPanelWidth, this.height);
        this.addRenderableWidget(this.configListWidget);

        this.addRenderableWidget(new RedCrossButton(this.width - 24, 4, 20, 20, this::onClose));
    }

    public void setSelectedMod(String modName) {
        if (this.configListWidget != null) {
            this.configListWidget.setMod(modName);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int leftPanelWidth = 140;
        graphics.fill(leftPanelWidth, 0, leftPanelWidth + 1, this.height, 0xFF555555);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parentScreen);
        }
    }

    private static class RedCrossButton extends AbstractWidget {
        private final Runnable pressAction;

        public RedCrossButton(int x, int y, int width, int height, Runnable pressAction) {
            super(x, y, width, height, Component.empty());
            this.pressAction = pressAction;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = mouseX >= this.getX() && mouseX < this.getX() + this.width 
                    && mouseY >= this.getY() && mouseY < this.getY() + this.height;

            int fillBg = hovered ? 0xFFFF2222 : 0xFFCC0000;
            
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fillBg);
            graphics.outline(this.getX(), this.getY(), this.width, this.height, 0xFFFFFFFF);
            
            graphics.centeredText(Minecraft.getInstance().font, "X", 
                this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
            if (this.isActive() && this.isMouseOver(event.x(), event.y())) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.pressAction.run();
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}