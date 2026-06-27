package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DLibMainScreen extends Screen {
    private final Screen parentScreen;
    private ModListWidget modListWidget;
    private ConfigListWidget configListWidget;
    private final List<ControlSquareButton> controlButtons = new ArrayList<>();

    public DLibMainScreen(Screen parentScreen) {
        super(Component.literal("EasyConfig Config Manager"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        ConfigManager.load(); 

        int leftPanelWidth = 140;
        this.controlButtons.clear();

        this.modListWidget = new ModListWidget(this, 0, 0, leftPanelWidth, this.height);
        this.addRenderableWidget(this.modListWidget);

        this.configListWidget = new ConfigListWidget(leftPanelWidth, 0, this.width - leftPanelWidth, this.height);
        this.addRenderableWidget(this.configListWidget);

        int btnX = this.width - 24;
        
        // 1. Discard Changes & Close Screen Button (Red Outline)
        ControlSquareButton discardCloseBtn = new ControlSquareButton(btnX, 4, 20, 20, "✖", 0xFFFF5555, 0xFFCC0000, 0xFFFF2222, () -> {
            if (this.configListWidget != null) this.configListWidget.discardCurrentModChanges();
            this.onClose();
        });
        this.controlButtons.add(discardCloseBtn);
        this.addRenderableWidget(discardCloseBtn);
        
        // 2. Save Settings Button (Green Outline)
        ControlSquareButton saveBtn = new ControlSquareButton(btnX, 28, 20, 20, "✔", 0xFF55FF55, 0xFF00AA00, 0xFF22FF22, () -> {
            if (this.configListWidget != null) this.configListWidget.saveCurrentMod();
        });
        this.controlButtons.add(saveBtn);
        this.addRenderableWidget(saveBtn);
        
        // 3. Reset to Factory Defaults Button (Blue Outline)
        ControlSquareButton defaultBtn = new ControlSquareButton(btnX, 52, 20, 20, "⟲", 0xFF5555FF, 0xFF0000CC, 0xFF2222FF, () -> {
            if (this.configListWidget != null) this.configListWidget.resetCurrentModDefaults();
        });
        this.controlButtons.add(defaultBtn);
        this.addRenderableWidget(defaultBtn);
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

        if (this.configListWidget != null && this.configListWidget.getCurrentMod() != null) {
            int panelLeft = this.width - 28;
            int panelBottom = 76;
            graphics.fill(panelLeft, 0, panelLeft + 1, panelBottom, 0xFF555555);
            graphics.fill(panelLeft, panelBottom, this.width, panelBottom + 1, 0xFF555555);
            graphics.fill(panelLeft + 1, 0, this.width, panelBottom, 0x22000000);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Propagate scroll wheel ticks down to viewports
        if (this.modListWidget != null && this.modListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (this.configListWidget != null && this.configListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.configListWidget != null && this.configListWidget.getCurrentMod() != null) {
            for (ControlSquareButton btn : controlButtons) {
                if (btn.mouseClicked(event, doubleClick)) return true;
            }
        }
        if (this.modListWidget != null && this.modListWidget.mouseClicked(event, doubleClick)) return true;
        if (this.configListWidget != null && this.configListWidget.mouseClicked(event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (event.isEscape()) { 
            if (this.configListWidget != null && this.configListWidget.getCurrentMod() != null) {
                this.configListWidget.discardCurrentModChanges();
            }
            this.onClose();
            return true;
        }
        return this.configListWidget != null && this.configListWidget.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return this.configListWidget != null && this.configListWidget.charTyped(event) || super.charTyped(event);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.gui.setScreen(this.parentScreen);
    }

    private class ControlSquareButton extends AbstractWidget {
        private final String label;
        private final int outlineColor;
        private final int baseColor;
        private final int hoverColor;
        private final Runnable pressAction;

        public ControlSquareButton(int x, int y, int width, int height, String label, int outlineColor, int baseColor, int hoverColor, Runnable pressAction) {
            super(x, y, width, height, Component.empty());
            this.label = label;
            this.outlineColor = outlineColor;
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.pressAction = pressAction;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (DLibMainScreen.this.configListWidget == null || DLibMainScreen.this.configListWidget.getCurrentMod() == null) return;
            boolean hovered = mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, hovered ? this.hoverColor : this.baseColor);
            graphics.outline(this.getX(), this.getY(), this.width, this.height, this.outlineColor);
            graphics.centeredText(Minecraft.getInstance().font, this.label, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
            if (DLibMainScreen.this.configListWidget == null || DLibMainScreen.this.configListWidget.getCurrentMod() == null) return false;
            if (this.isActive()) {
                double mx = event.x(); double my = event.y();
                if (mx >= this.getX() && mx < this.getX() + this.width && my >= this.getY() && my < this.getY() + this.height) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.pressAction.run();
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput n) {}
    }
}