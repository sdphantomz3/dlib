package com.phantom.dlib.client.gui;

import com.phantom.dlib.client.config.ConfigManager;
import com.phantom.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ConfigListWidget extends AbstractWidget {
    private String currentMod = null;
    private final List<AbstractWidget> dynamicUiWidgets = new ArrayList<>();
    private final List<CategoryLabel> categoryLabels = new ArrayList<>();

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
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
                        ConfigManager.saveMod(this.currentMod);
                        b.setMessage(Component.literal(key + ": " + (state ? "ON" : "OFF")));
                    }).bounds(this.getX() + 25, currentYOffset, 180, 20).build();
                    this.dynamicUiWidgets.add(btn);

                } else if (option.type.equals("cycle")) {
                    Button btn = Button.builder(Component.literal(key + ": " + option.value), (b) -> {
                        int index = option.choices.indexOf(option.value);
                        int nextIndex = (index + 1) % option.choices.size();
                        option.value = option.choices.get(nextIndex);
                        ConfigManager.saveMod(this.currentMod);
                        b.setMessage(Component.literal(key + ": " + option.value));
                    }).bounds(this.getX() + 25, currentYOffset, 180, 20).build();
                    this.dynamicUiWidgets.add(btn);

                } else if (option.type.equals("input")) {
                    this.categoryLabels.add(new CategoryLabel(key + ":", this.getX() + 25, currentYOffset + 4, 0xFFBBBBBB, 1.0f));
                    
                    EditBox inputField = new EditBox(mc.font, this.getX() + 110, currentYOffset, 120, 18, Component.empty());
                    inputField.setValue(option.value);
                    inputField.setResponder((newValue) -> {
                        option.value = newValue;
                        ConfigManager.saveMod(this.currentMod);
                    });
                    this.dynamicUiWidgets.add(inputField);
                }
                currentYOffset += 24;
            }
            currentYOffset += 10; 
        }
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

    // --- WRAPPED KEY EVENT PIPELINE ---
    public boolean keyPressed(final KeyEvent event) {
        for (AbstractWidget widget : dynamicUiWidgets) {
            if (widget instanceof EditBox && widget.isFocused()) {
                return widget.keyPressed(event);
            }
        }
        return false;
    }

    // --- WRAPPED CHARACTER EVENT PIPELINE ---
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