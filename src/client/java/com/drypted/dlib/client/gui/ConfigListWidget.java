package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
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
    private final List<ConfigRow> configRows = new ArrayList<>();
    
    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    private String feedbackMessage = "";
    private long feedbackExpiryTime = 0L;
    private int feedbackColor = 0xFFFFFFFF;

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public String getCurrentMod() { return this.currentMod; }

    public void setMod(String modName) {
        this.currentMod = modName;
        this.configRows.clear();
        this.scrollAmount = 0;

        if (modName == null) return;

        Minecraft mc = Minecraft.getInstance();
        LinkedHashMap<String, LinkedHashMap<String, ConfigManager.ConfigOption>> structure = ConfigManager.getStructureForMod(modName);
        
        int runningRelativeY = 0;

        for (Map.Entry<String, LinkedHashMap<String, ConfigManager.ConfigOption>> catEntry : structure.entrySet()) {
            configRows.add(new ConfigRow(catEntry.getKey(), null, null, true, runningRelativeY, 20));
            runningRelativeY += 20;

            for (Map.Entry<String, ConfigManager.ConfigOption> optEntry : catEntry.getValue().entrySet()) {
                final String key = optEntry.getKey();
                final ConfigManager.ConfigOption option = optEntry.getValue();
                AbstractWidget inputWidget = null;
                int targetWidgetX = this.getX() + this.width - 170;

                if (option.type.equals("toggle")) {
                    inputWidget = Button.builder(Component.literal(Boolean.parseBoolean(option.value) ? "ON" : "OFF"), (b) -> {
                        boolean state = !Boolean.parseBoolean(option.value);
                        option.value = String.valueOf(state);
                        b.setMessage(Component.literal(state ? "ON" : "OFF"));
                    }).bounds(targetWidgetX, 0, 130, 20).build();

                } else if (option.type.equals("cycle")) {
                    inputWidget = Button.builder(Component.literal(option.value), (b) -> {
                        int index = option.choices.indexOf(option.value);
                        int nextIndex = (index + 1) % option.choices.size();
                        option.value = option.choices.get(nextIndex);
                        b.setMessage(Component.literal(option.value));
                    }).bounds(targetWidgetX, 0, 130, 20).build();

                } else if (option.type.equals("text") || option.type.equals("number")) {
                    EditBox inputField = new EditBox(mc.font, targetWidgetX, 0, 130, 18, Component.empty());
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
                    inputWidget = inputField;
                }

                configRows.add(new ConfigRow(null, key, inputWidget, false, runningRelativeY, 24));
                runningRelativeY += 24;
            }
            runningRelativeY += 8; 
        }
        this.totalContentHeight = runningRelativeY;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.currentMod == null) return false;
        int viewTop = this.getY() + 15;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;
        
        if (this.totalContentHeight <= viewHeight) return false;

        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
            int maxScroll = this.totalContentHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - scrollY * 14, maxScroll));
            return true;
        }
        return false;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (this.currentMod == null) {
            RenderUtil.drawScaledText(graphics, "Select a mod from the left sidebar.", 1.0f, this.getX() + this.width / 4, this.getY() + this.height / 2, 0xFFAAAAAA, 1.0f, false);
            return;
        }

        RenderUtil.drawScaledText(graphics, "Mod Config: " + this.currentMod, 1.3f, this.getX() + 20, this.getY() + 18, 0xFFFFFFFF, 1.0f, true);

        int viewTop = this.getY() + 45;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;

        for (ConfigRow row : configRows) {
            int rowScreenY = viewTop + row.relativeY - (int)scrollAmount;
            if (rowScreenY + row.height < viewTop || rowScreenY > viewBottom) continue;

            if (row.isCategory) {
                RenderUtil.drawScaledText(graphics, row.categoryName, 1.1f, this.getX() + 20, rowScreenY + 2, 0xFFFFCC00, 1.0f, true);
            } else {
                int widgetX = this.getX() + this.width - 170;
                int maxLabelWidth = (widgetX - (this.getX() + 20)) - 10;
                
                String cleanLabelText = row.optionKey + ":";
                if (mc.font.width(cleanLabelText) > maxLabelWidth) {
                    cleanLabelText = mc.font.plainSubstrByWidth(cleanLabelText, maxLabelWidth - 8) + "...";
                }

                // FIXED: Replaced undefined graphics.drawString with RenderUtil.drawScaledText
                RenderUtil.drawScaledText(graphics, cleanLabelText, 1.0f, this.getX() + 20, rowScreenY + 5, 0xFFBBBBBB, 1.0f, true);

                if (row.widget != null) {
                    row.widget.setY(rowScreenY + (row.height - row.widget.getHeight()) / 2);
                    row.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
                }
            }
        }

        if (totalContentHeight > viewHeight) {
            int scrollbarX = this.getX() + this.width - 6;
            int trackWidth = 4;
            int thumbHeight = Math.max(12, (viewHeight * viewHeight) / totalContentHeight);
            int maxScroll = totalContentHeight - viewHeight;
            int thumbY = viewTop + (int)((scrollAmount / maxScroll) * (viewHeight - thumbHeight));

            graphics.fill(scrollbarX, viewTop, scrollbarX + trackWidth, viewBottom, 0x22FFFFFF); 
            graphics.fill(scrollbarX, thumbY, scrollbarX + trackWidth, thumbY + thumbHeight, 0x88FFFFFF); 
        }

        if (Util.getMillis() < this.feedbackExpiryTime && !this.feedbackMessage.isEmpty()) {
            RenderUtil.drawScaledText(graphics, "● " + this.feedbackMessage, 1.0f, this.getX() + this.width - 110, this.getY() + this.height - 18, this.feedbackColor, 1.0f, true);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.currentMod == null) return false;
        
        double mx = event.x(); double my = event.y();
        int viewTop = this.getY() + 45;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;

        if (totalContentHeight > viewHeight && mx >= this.getX() + this.width - 10 && mx <= this.getX() + this.width && my >= viewTop && my <= viewBottom) {
            double clickPercentage = (my - viewTop) / (double)viewHeight;
            int maxScroll = totalContentHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(clickPercentage * maxScroll, maxScroll));
            return true;
        }

        for (ConfigRow row : configRows) {
            if (row.isCategory || row.widget == null) continue;
            int rowScreenY = viewTop + row.relativeY - (int)scrollAmount;
            if (rowScreenY + row.height < viewTop || rowScreenY > viewBottom) continue;

            if (row.widget.mouseClicked(event, doubleClick)) {
                if (row.widget instanceof EditBox) {
                    for (ConfigRow r : configRows) {
                        if (r.widget instanceof EditBox) ((EditBox) r.widget).setFocused(r.widget == row.widget);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(final KeyEvent event) {
        for (ConfigRow r : configRows) {
            if (r.widget instanceof EditBox && r.widget.isFocused()) return r.widget.keyPressed(event);
        }
        return false;
    }

    public boolean charTyped(final CharacterEvent event) {
        for (ConfigRow r : configRows) {
            if (r.widget instanceof EditBox && r.widget.isFocused()) return r.widget.charTyped(event);
        }
        return false;
    }

    public void saveCurrentMod() {
        if (this.currentMod != null) {
            ConfigManager.saveMod(this.currentMod);
            this.triggerFeedback("Saved!", 0xFF55FF55);
        }
    }

    public void discardCurrentModChanges() {
        if (this.currentMod != null) {
            ConfigManager.discardChanges(this.currentMod);
            this.setMod(this.currentMod);
            this.triggerFeedback("Changes Reverted", 0xFFFF5555);
        }
    }

    public void resetCurrentModDefaults() {
        if (this.currentMod != null) {
            ConfigManager.resetToDefaults(this.currentMod);
            this.setMod(this.currentMod);
            this.triggerFeedback("Defaults Applied", 0xFF5555FF);
        }
    }

    private void triggerFeedback(String message, int color) {
        this.feedbackMessage = message;
        this.feedbackColor = color;
        this.feedbackExpiryTime = Util.getMillis() + 2500;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private static class ConfigRow {
        final String categoryName;
        final String optionKey;
        final AbstractWidget widget;
        final boolean isCategory;
        final int relativeY;
        final int height;

        ConfigRow(String catName, String key, AbstractWidget widget, boolean isCategory, int relativeY, int height) {
            this.categoryName = catName;
            this.optionKey = key;
            this.widget = widget;
            this.isCategory = isCategory;
            this.relativeY = relativeY;
            this.height = height;
        }
    }
}