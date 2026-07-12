package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DLibMainScreen extends Screen {
    private final Screen parentScreen;
    private ModListWidget modListWidget;
    private ConfigListWidget configListWidget;
    private final String initialModId; 

    public DLibMainScreen(Screen parentScreen, String initialModId) {
        super(Component.literal("EasyConfig Config Manager"));
        this.parentScreen = parentScreen;
        this.initialModId = initialModId;
    }
    
    public DLibMainScreen(Screen parentScreen) {
        this(parentScreen, null);   // no initial mod preselected
    }

    @Override
    protected void init() {
        // Preserve current mod selection across re-initializations (e.g. popup closes)
        String previousMod = this.configListWidget != null ? this.configListWidget.getCurrentMod() : null;

        // Only load from disk on first init; re-init (e.g. returning from popup)
        // must NOT reload or it will overwrite in-memory changes made by the popup.
        if (previousMod == null) {
            ConfigManager.load();
        }

        int leftPanelWidth = 140;

        // Reuse existing widget instances to preserve their internal state
        // (category expanded/collapsed, scroll position, etc.) across re-inits.
        if (this.modListWidget == null) {
            this.modListWidget = new ModListWidget(this, 0, 0, leftPanelWidth, this.height);
        } else {
            this.modListWidget.setWidth(leftPanelWidth);
            this.modListWidget.setHeight(this.height);
        }
        this.addRenderableWidget(this.modListWidget);

        if (this.configListWidget == null) {
            this.configListWidget = new ConfigListWidget(leftPanelWidth, 0, this.width - leftPanelWidth, this.height);
        } else {
            this.configListWidget.setWidth(this.width - leftPanelWidth);
            this.configListWidget.setHeight(this.height);
            this.configListWidget.setX(leftPanelWidth);
            this.configListWidget.setY(0);
        }
        this.addRenderableWidget(this.configListWidget);

        if (previousMod != null) {
            // Only rebuild rows if needed — setMod on the same instance preserves category state
            this.configListWidget.refreshForSameMod();
        } else if (initialModId != null && !initialModId.isEmpty()) {
            setSelectedMod(initialModId);
        }
    }
    
    public static Screen openConfigScreen(Screen parent, String modId) {
        return new DLibMainScreen(parent, modId);
    }

    public void setSelectedMod(String modName) {
        if (this.configListWidget != null) {
            this.configListWidget.setMod(modName);
        }
        if (this.modListWidget != null) {
            this.modListWidget.setSelectedMod(modName);
        }
    }

    /** Called by ModListWidget action bar — save current mod config */
    public void onSave() {
        if (this.configListWidget != null) this.configListWidget.saveCurrentMod();
    }

    /** Called by ModListWidget action bar — discard unsaved changes and close */
    public void onDiscard() {
        if (this.configListWidget != null) this.configListWidget.discardCurrentModChanges();
        this.onClose();
    }

    /** Called by ModListWidget action bar — reset to factory defaults */
    public void onResetDefaults() {
        if (this.configListWidget != null) this.configListWidget.resetCurrentModDefaults();
    }

    /** Whether a mod is currently selected (used to show/hide action bar) */
    public boolean hasModSelected() {
        return this.configListWidget != null && this.configListWidget.getCurrentMod() != null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int leftPanelWidth = 140;
        // Vertical divider between the two panels
        graphics.fill(leftPanelWidth, 0, leftPanelWidth + 1, this.height, 0xFF555555);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.modListWidget != null && this.modListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (this.configListWidget != null && this.configListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
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
}