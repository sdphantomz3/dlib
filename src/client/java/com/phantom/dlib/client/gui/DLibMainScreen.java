package com.phantom.dlib.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
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
        int leftPanelWidth = 140;

        // Create Left List Pane
        this.modListWidget = new ModListWidget(this, 0, 0, leftPanelWidth, this.height);
        this.addRenderableWidget(this.modListWidget);

        // Create Right Detail Pane
        this.configListWidget = new ConfigListWidget(leftPanelWidth, 0, this.width - leftPanelWidth, this.height);
        this.addRenderableWidget(this.configListWidget);
    }

    public void setSelectedMod(String modName) {
        if (this.configListWidget != null) {
            this.configListWidget.setMod(modName);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Super automatically processes the state extraction of child widgets added via addRenderableWidget
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // Draw a clean vertical dividing stroke separating left pane from right pane
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
}