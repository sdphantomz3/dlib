package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.config.ConfigManager;
import com.phantom.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.*;
import java.util.function.Consumer;

public class ConfigListWidget extends AbstractWidget {
    private String currentMod = null;
    private final List<AbstractWidget> dynamicUiWidgets = new ArrayList<>();
    private final List<CategoryLabel> categoryLabels = new ArrayList<>();

    // --- TIMING CONFIRMATION SYSTEM CORNER STATE ---
    private String feedbackMessage = "";
    private long feedbackExpiryTime = 0L;
    private int feedbackColor = 0xFFFFFFFF;

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public String getCurrentMod() {
        return this.currentMod;
    }
    
    public void setMod(String modName) {
        this.currentMod = modName;
        this.dynamicUiWidgets.clear();
        this.categoryLabels.clear();

        if (modName == null) return;

        Minecraft mc = Minecraft.getInstance();
        LinkedHashMap<String, LinkedHashMap<String, ConfigManager.ConfigOption>> structure = ConfigManager.getStructureForMod(modName);
        int currentYOffset = this.getY() + 50;

        for (Map.Entry<String, LinkedHashMap<String, ConfigManager.ConfigOption>> catEntry : structure.entrySet()) {
            this.categoryLabels.add(new CategoryLabel(catEntry.getKey(), this.getX() + 20, currentYOffset));
            currentYOffset += 18;

            for (Map.Entry<String, ConfigManager.ConfigOption> optEntry : catEntry.getValue().entrySet()) {
                final String key = optEntry.getKey();
                final ConfigManager.ConfigOption option = optEntry.getValue();

                if (option.type.equals("toggle")) {
                    Button btn = Button.builder(Component.literal(key + ": " + (Boolean.parseBoolean(option.value) ? "ON" : "OFF")), (b) -> {
                        boolean state = !Boolean.parseBoolean(option.value);
                        option.value = String.valueOf(state);
                        b.setMessage(Component.literal(key + ": " + (state ? "ON" : "OFF")));
                    }).bounds(this.getX() + 25, currentYOffset, 180, 20).build();
                    this.dynamicUiWidgets.add(btn);

                } else if (option.type.equals("cycle")) {
                    Button btn = Button.builder(Component.literal(key + ": " + option.value), (b) -> {
                        int index = option.choices.indexOf(option.value);
                        int nextIndex = (index + 1) % option.choices.size();
                        option.value = option.choices.get(nextIndex);
                        b.setMessage(Component.literal(key + ": " + option.value));
                    }).bounds(this.getX() + 25, currentYOffset, 180, 20).build();
                    this.dynamicUiWidgets.add(btn);

                } else if (option.type.equals("text") || option.type.equals("number")) {
                    this.categoryLabels.add(new CategoryLabel(key + ":", this.getX() + 25, currentYOffset + 4, 0xFFBBBBBB, 1.0f));
                    
                    final EditBox inputField = new EditBox(mc.font, this.getX() + 110, currentYOffset, 120, 18, Component.empty());
                    inputField.setValue(option.value);
                    
                    inputField.setResponder(new Consumer<String>() {
                        private String lastValidValue = option.value;
                        private boolean isReverting = false;

                        @Override
                        public void accept(String newValue) {
                            if (isReverting) return;

                            if (option.type.equals("number")) {
                                if (!newValue.isEmpty() && !newValue.equals("-") && !newValue.equals(".") && !newValue.equals("-.") && !newValue.matches("^-?\\d*\\.?\\d*$")) {
                                    isReverting = true;
                                    inputField.setValue(lastValidValue);
                                    isReverting = false;
                                    return;
                                }
                            }
                            lastValidValue = newValue;
                            option.value = newValue;
                        }
                    });

                    this.dynamicUiWidgets.add(inputField);
                }
                currentYOffset += 24;
            }
            currentYOffset += 10; 
        }
    }

    // --- BRIDGE INTERFACE INVOKED FROM PANEL HOOKS ---

    public void saveCurrentMod() {
        if (this.currentMod != null) {
            ConfigManager.saveMod(this.currentMod);
            this.triggerFeedback("Saved to JSON!", 0xFF55FF55); // Green Text
        }
    }

    public void discardCurrentModChanges() {
        if (this.currentMod != null) {
            ConfigManager.discardChanges(this.currentMod);
            this.setMod(this.currentMod); // Force re-sync
            this.triggerFeedback("Changes Reverted", 0xFFFF5555); // Red Text
        }
    }

    public void resetCurrentModDefaults() {
        if (this.currentMod != null) {
            ConfigManager.resetToDefaults(this.currentMod);
            this.setMod(this.currentMod); // Force updates
            this.triggerFeedback("Defaults Applied", 0xFF5555FF); // Blue Text
        }
    }

    private void triggerFeedback(String message, int color) {
        this.feedbackMessage = message;
        this.feedbackColor = color;
        this.feedbackExpiryTime = Util.getMillis() + 2500;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (this.currentMod == null) {
            graphics.centeredText(mc.font, "Select a mod from the left sidebar.", this.getX() + this.width / 2, this.getY() + this.height / 2, 0xFFAAAAAA);
            return;
        }

        RenderUtil.drawScaledText(graphics, "Mod Config: " + this.currentMod, 1.3f, this.getX() + 20, this.getY() + 18, 0xFFFFFFFF, 1.0f, true);

        for (CategoryLabel label : categoryLabels) {
            RenderUtil.drawScaledText(graphics, label.text, label.scale, label.x, label.y, label.color, 1.0f, true);
        }

        for (AbstractWidget widget : dynamicUiWidgets) {
            widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        // --- RENDER VISUAL ACTION CONFIRMATION STATUS ---
        if (Util.getMillis() < this.feedbackExpiryTime && !this.feedbackMessage.isEmpty()) {
            // Positioned clearly in the bottom-right workspace viewport
            int renderX = this.getX() + this.width - 110;
            int renderY = this.getY() + this.height - 18;
            graphics.centeredText(mc.font, "● " + this.feedbackMessage, renderX, renderY, this.feedbackColor);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.currentMod != null) {
            for (AbstractWidget widget : dynamicUiWidgets) {
                if (widget.mouseClicked(event, doubleClick)) {
                    if (widget instanceof EditBox) {
                        for (AbstractWidget w : dynamicUiWidgets) if (w instanceof EditBox) ((EditBox) w).setFocused(w == widget);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean keyPressed(final KeyEvent event) {
        for (AbstractWidget widget : dynamicUiWidgets) {
            if (widget instanceof EditBox && widget.isFocused()) {
                return widget.keyPressed(event);
            }
        }
        return false;
    }

    public boolean charTyped(final CharacterEvent event) {
        for (AbstractWidget widget : dynamicUiWidgets) {
            if (widget instanceof EditBox && widget.isFocused()) {
                return widget.charTyped(event);
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private static class CategoryLabel {
        String text; int x; int y; int color; float scale;
        CategoryLabel(String text, int x, int y) { this(text, x, y, 0xFFFFCC00, 1.1f); }
        CategoryLabel(String text, int x, int y, int color, float scale) {
            this.text = text; this.x = x; this.y = y; this.color = color; this.scale = scale;
        }
    }
}