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
    private final Map<String, Boolean> categoryExpanded = new HashMap<>();
    private List<CategoryInfo> categoryInfos = new ArrayList<>();

    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    // ── Toast notification ───────────────────────────────────────────────────
    // The toast slides in from the right edge, stays, then slides back out.
    //
    // Timeline (all in ms, configurable):
    //   0                     → starts sliding in
    //   TOAST_SLIDE_MS        → fully visible, hold begins
    //   TOAST_SLIDE_MS + TOAST_HOLD_MS          → starts sliding back out
    //   TOAST_SLIDE_MS*2 + TOAST_HOLD_MS        → fully hidden (animation done)
    //
    private static final long TOAST_SLIDE_MS = 300L;  // slide in / slide out duration
    private static final long TOAST_HOLD_MS  = 1800L; // how long it stays visible

    // Toast visual constants
    private static final int TOAST_H        = 22;
    private static final int TOAST_PAD_X    = 10;
    private static final int TOAST_PAD_Y    = 6;
    private static final int TOAST_MARGIN   = 10; // gap from right + bottom edges

    // MC panel-style colours (matches the dark inventory panel look)
    private static final int TOAST_BG       = 0xFF2D2D2D;
    private static final int TOAST_BORDER_L = 0xFF555555; // light edge (top+left)
    private static final int TOAST_BORDER_D = 0xFF111111; // dark edge (bottom+right)

    private String  toastMessage  = "";
    private int     toastColor    = 0xFFFFFFFF;
    private long    toastStartMs  = 0L;          // Util.getMillis() when triggerFeedback was called
    private boolean toastActive   = false;

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public String getCurrentMod() { return this.currentMod; }

    // ── Mod loading ──────────────────────────────────────────────────────────

    public void setMod(String modName) {
        this.currentMod = modName;
        this.scrollAmount = 0;
        this.categoryInfos.clear();
        this.categoryExpanded.clear();

        if (modName == null) {
            this.configRows.clear();
            this.totalContentHeight = 0;
            return;
        }

        LinkedHashMap<String, LinkedHashMap<String, ConfigManager.ConfigOption>> structure =
                ConfigManager.getStructureForMod(modName);

        for (Map.Entry<String, LinkedHashMap<String, ConfigManager.ConfigOption>> catEntry : structure.entrySet()) {
            String catName = catEntry.getKey();
            List<OptionInfo> options = new ArrayList<>();
            for (Map.Entry<String, ConfigManager.ConfigOption> optEntry : catEntry.getValue().entrySet()) {
                options.add(new OptionInfo(optEntry.getKey(), optEntry.getValue()));
            }
            categoryInfos.add(new CategoryInfo(catName, options));
            categoryExpanded.put(catName, false);
        }

        buildRows();
    }

    private void buildRows() {
        configRows.clear();
        int runningRelativeY = 0;
        Minecraft mc = Minecraft.getInstance();

        for (CategoryInfo cat : categoryInfos) {
            configRows.add(new ConfigRow(cat.name, null, null, true, runningRelativeY, 20));
            runningRelativeY += 20;

            if (categoryExpanded.getOrDefault(cat.name, false)) {
                for (OptionInfo optInfo : cat.options) {
                    final String key = optInfo.key;
                    final ConfigManager.ConfigOption option = optInfo.option;
                    AbstractWidget inputWidget = null;
                    int targetWidgetX = this.getX() + this.width - 170;

                    if (option.type.equals("toggle")) {
                        inputWidget = Button.builder(
                                Component.literal(Boolean.parseBoolean(option.value) ? "ON" : "OFF"),
                                (b) -> {
                                    boolean state = !Boolean.parseBoolean(option.value);
                                    option.value = String.valueOf(state);
                                    b.setMessage(Component.literal(state ? "ON" : "OFF"));
                                })
                                .bounds(targetWidgetX, 0, 130, 20)
                                .build();
                    } else if (option.type.equals("cycle")) {
                        inputWidget = Button.builder(
                                Component.literal(option.value),
                                (b) -> {
                                    int index = option.choices.indexOf(option.value);
                                    int nextIndex = (index + 1) % option.choices.size();
                                    option.value = option.choices.get(nextIndex);
                                    b.setMessage(Component.literal(option.value));
                                })
                                .bounds(targetWidgetX, 0, 130, 20)
                                .build();
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
                                    if (!newValue.isEmpty() && !newValue.equals("-") && !newValue.equals(".") &&
                                            !newValue.equals("-.") && !newValue.matches("^-?\\d*\\.?\\d*$")) {
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
            } else {
                runningRelativeY += 4;
            }
        }
        this.totalContentHeight = runningRelativeY;
    }

    private void toggleCategory(String catName) {
        boolean current = categoryExpanded.getOrDefault(catName, false);
        categoryExpanded.put(catName, !current);
        buildRows();
    }

    // ── Scroll ───────────────────────────────────────────────────────────────

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.currentMod == null) return false;
        int viewTop = this.getY() + 15;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;
        if (this.totalContentHeight <= viewHeight) return false;
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width &&
                mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
            int maxScroll = this.totalContentHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - scrollY * 14, maxScroll));
            return true;
        }
        return false;
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (this.currentMod == null) {
            RenderUtil.drawScaledText(graphics, "Select a mod from the left sidebar.",
                    1.0f, this.getX() + this.width / 4, this.getY() + this.height / 2,
                    0xFFAAAAAA, 1.0f, false);
            return;
        }

        String displayName = ConfigManager.getModDisplayName(currentMod);
        RenderUtil.drawScaledText(graphics, "Mod Config: " + displayName,
                1.3f, this.getX() + 20, this.getY() + 18, 0xFFFFFFFF, 1.0f, true);

        int viewTop    = this.getY() + 45;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;

        for (ConfigRow row : configRows) {
            int rowScreenY = viewTop + row.relativeY - (int) scrollAmount;
            if (rowScreenY + row.height < viewTop || rowScreenY > viewBottom) continue;

            if (row.isCategory) {
                boolean expanded = categoryExpanded.getOrDefault(row.categoryName, true);
                boolean isHovered = mouseX >= this.getX() + 10 && mouseX <= this.getX() + this.width - 10 &&
                                    mouseY >= rowScreenY && mouseY <= rowScreenY + row.height;
                if (isHovered) {
                    graphics.fill(this.getX() + 10, rowScreenY, this.getX() + this.width - 10, rowScreenY + row.height, 0x33FFFFFF);
                }
                int color = isHovered ? 0xFFFFFF55 : 0xFFFFCC00;
                int size = 9;
                int tx = this.getX() + 22;
                int ty = rowScreenY + (20 - size) / 2;
                if (expanded) {
                    for (int i = 0; i < size; i++) {
                        int w = size - i;
                        int offset = i / 2;
                        graphics.fill(tx + offset, ty + i, tx + offset + w, ty + i + 1, color);
                    }
                } else {
                    for (int c = 0; c < size; c++) {
                        int h = size - c;
                        int sy = ty + c / 2;
                        graphics.fill(tx + c, sy, tx + c + 1, sy + h, color);
                    }
                }
                RenderUtil.drawScaledText(graphics, row.categoryName,
                        1.1f, this.getX() + 40, rowScreenY + 5, color, 1.0f, true);
            } else {
                int widgetX = this.getX() + this.width - 170;
                int maxLabelWidth = (widgetX - (this.getX() + 20)) - 10;
                String cleanLabelText = row.optionKey + ":";
                if (mc.font.width(cleanLabelText) > maxLabelWidth) {
                    cleanLabelText = mc.font.plainSubstrByWidth(cleanLabelText, maxLabelWidth - 8) + "...";
                }
                RenderUtil.drawScaledText(graphics, cleanLabelText,
                        1.0f, this.getX() + 20, rowScreenY + 5, 0xFFBBBBBB, 1.0f, true);
                if (row.widget != null) {
                    row.widget.setY(rowScreenY + (row.height - row.widget.getHeight()) / 2);
                    row.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
                }
            }
        }

        // Scrollbar
        if (totalContentHeight > viewHeight) {
            int scrollbarX = this.getX() + this.width - 6;
            int trackWidth = 4;
            int thumbHeight = Math.max(12, (viewHeight * viewHeight) / totalContentHeight);
            int maxScroll = totalContentHeight - viewHeight;
            int thumbY = viewTop + (int) ((scrollAmount / maxScroll) * (viewHeight - thumbHeight));
            graphics.fill(scrollbarX, viewTop, scrollbarX + trackWidth, viewBottom, 0x22FFFFFF);
            graphics.fill(scrollbarX, thumbY, scrollbarX + trackWidth, thumbY + thumbHeight, 0x88FFFFFF);
        }

        // Toast notification
        drawToast(graphics);
    }

    // ── Toast ─────────────────────────────────────────────────────────────────

    /**
     * Draws the sliding toast.
     *
     * Slide offset logic:
     *   phase 0 (0..SLIDE_MS)               → sliding in:  offset = toastWidth * (1 - t)
     *   phase 1 (SLIDE_MS..SLIDE_MS+HOLD_MS) → fully shown: offset = 0
     *   phase 2 (..SLIDE_MS*2+HOLD_MS)       → sliding out: offset = toastWidth * t
     *   after that                            → hidden, toastActive = false
     */
    private void drawToast(GuiGraphicsExtractor g) {
        if (!toastActive) return;

        long now     = Util.getMillis();
        long elapsed = now - toastStartMs;
        long total   = TOAST_SLIDE_MS * 2 + TOAST_HOLD_MS;

        if (elapsed >= total) {
            toastActive = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int textW  = mc.font.width(toastMessage);
        int toastW = textW + TOAST_PAD_X * 2;

        // Compute horizontal slide offset (pixels hidden to the right)
        float slideOffset;
        if (elapsed < TOAST_SLIDE_MS) {
            // Sliding in: ease-out (1 - t^2 reversed → start fast, slow down)
            float t = (float) elapsed / TOAST_SLIDE_MS;
            slideOffset = toastW * (1f - easeOut(t));
        } else if (elapsed < TOAST_SLIDE_MS + TOAST_HOLD_MS) {
            slideOffset = 0f;
        } else {
            // Sliding out
            float t = (float) (elapsed - TOAST_SLIDE_MS - TOAST_HOLD_MS) / TOAST_SLIDE_MS;
            slideOffset = toastW * easeIn(t);
        }

        // Position: bottom-right of the right pane
        int rightEdge = this.getX() + this.width - TOAST_MARGIN;
        int toastX    = (int) (rightEdge - toastW + slideOffset);
        int toastY    = this.getY() + this.height - TOAST_MARGIN - TOAST_H;

        // Draw MC-panel style background
        // Dark fill
        g.fill(toastX + 1, toastY + 1, toastX + toastW - 1, toastY + TOAST_H - 1, TOAST_BG);
        // Light top + left edges (1px)
        g.fill(toastX,           toastY,           toastX + toastW,     toastY + 1,     TOAST_BORDER_L); // top
        g.fill(toastX,           toastY + 1,       toastX + 1,          toastY + TOAST_H - 1, TOAST_BORDER_L); // left
        // Dark bottom + right edges (1px)
        g.fill(toastX,           toastY + TOAST_H - 1, toastX + toastW, toastY + TOAST_H, TOAST_BORDER_D); // bottom
        g.fill(toastX + toastW - 1, toastY + 1,   toastX + toastW,    toastY + TOAST_H - 1, TOAST_BORDER_D); // right
        // Corner pixels — cut to a slight rounded look by darkening them
        g.fill(toastX,           toastY,           toastX + 1,          toastY + 1,     TOAST_BORDER_D); // top-left
        g.fill(toastX + toastW - 1, toastY,        toastX + toastW,     toastY + 1,     TOAST_BORDER_D); // top-right
        g.fill(toastX,           toastY + TOAST_H - 1, toastX + 1,      toastY + TOAST_H, TOAST_BORDER_D); // bot-left
        g.fill(toastX + toastW - 1, toastY + TOAST_H - 1, toastX + toastW, toastY + TOAST_H, TOAST_BORDER_D); // bot-right

        // Accent left bar (1px wide, inset 1 from left edge, colour matches message type)
        g.fill(toastX + 1, toastY + 1, toastX + 3, toastY + TOAST_H - 1, toastColor);

        // Text centred vertically, offset right of accent bar
        int textY = toastY + (TOAST_H - 8) / 2;
        RenderUtil.drawScaledText(g, toastMessage, 1.0f,
                toastX + TOAST_PAD_X + 2, textY, 0xFFEEEEEE, 1.0f, true);
    }

    /** Quadratic ease-out: starts fast, ends slow. t in [0,1] → [0,1]. */
    private static float easeOut(float t) { return 1f - (1f - t) * (1f - t); }

    /** Quadratic ease-in: starts slow, ends fast. t in [0,1] → [0,1]. */
    private static float easeIn(float t)  { return t * t; }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.currentMod == null) return false;
        double mx = event.x(), my = event.y();
        int viewTop    = this.getY() + 45;
        int viewBottom = this.height - 15;
        int viewHeight = viewBottom - viewTop;

        if (totalContentHeight > viewHeight &&
                mx >= this.getX() + this.width - 10 && mx <= this.getX() + this.width &&
                my >= viewTop && my <= viewBottom) {
            double clickPercentage = (my - viewTop) / (double) viewHeight;
            int maxScroll = totalContentHeight - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(clickPercentage * maxScroll, maxScroll));
            return true;
        }

        for (ConfigRow row : configRows) {
            if (!row.isCategory) continue;
            int rowScreenY = viewTop + row.relativeY - (int) scrollAmount;
            if (rowScreenY + row.height < viewTop || rowScreenY > viewBottom) continue;
            if (mx >= this.getX() && mx < this.getX() + this.width &&
                    my >= rowScreenY && my < rowScreenY + row.height) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                toggleCategory(row.categoryName);
                return true;
            }
        }

        for (ConfigRow row : configRows) {
            if (row.isCategory || row.widget == null) continue;
            int rowScreenY = viewTop + row.relativeY - (int) scrollAmount;
            if (rowScreenY + row.height < viewTop || rowScreenY > viewBottom) continue;
            if (row.widget.mouseClicked(event, doubleClick)) {
                if (row.widget instanceof EditBox) {
                    for (ConfigRow r : configRows) {
                        if (r.widget instanceof EditBox) {
                            ((EditBox) r.widget).setFocused(r.widget == row.widget);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(final KeyEvent event) {
        for (ConfigRow r : configRows) {
            if (r.widget instanceof EditBox && r.widget.isFocused()) {
                return r.widget.keyPressed(event);
            }
        }
        return false;
    }

    public boolean charTyped(final CharacterEvent event) {
        for (ConfigRow r : configRows) {
            if (r.widget instanceof EditBox && r.widget.isFocused()) {
                return r.widget.charTyped(event);
            }
        }
        return false;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    public void saveCurrentMod() {
        if (this.currentMod != null) {
            ConfigManager.saveMod(this.currentMod);
            triggerFeedback("Saved!", 0xFF55FF55);
        }
    }

    public void discardCurrentModChanges() {
        if (this.currentMod != null) {
            ConfigManager.discardChanges(this.currentMod);
            this.setMod(this.currentMod);
            triggerFeedback("Changes Reverted", 0xFFFF5555);
        }
    }

    public void resetCurrentModDefaults() {
        if (this.currentMod != null) {
            ConfigManager.resetToDefaults(this.currentMod);
            this.setMod(this.currentMod);
            triggerFeedback("Defaults Applied", 0xFF5599FF);
        }
    }

    private void triggerFeedback(String message, int color) {
        this.toastMessage  = message;
        this.toastColor    = color;
        this.toastStartMs  = Util.getMillis();
        this.toastActive   = true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    // ── Data classes ─────────────────────────────────────────────────────────

    private static class CategoryInfo {
        final String name;
        final List<OptionInfo> options;
        CategoryInfo(String name, List<OptionInfo> options) { this.name = name; this.options = options; }
    }

    private static class OptionInfo {
        final String key;
        final ConfigManager.ConfigOption option;
        OptionInfo(String key, ConfigManager.ConfigOption option) { this.key = key; this.option = option; }
    }

    private static class ConfigRow {
        final String categoryName;
        final String optionKey;
        final AbstractWidget widget;
        final boolean isCategory;
        final int relativeY;
        final int height;
        ConfigRow(String catName, String key, AbstractWidget widget, boolean isCategory, int relativeY, int height) {
            this.categoryName = catName; this.optionKey = key; this.widget = widget;
            this.isCategory = isCategory; this.relativeY = relativeY; this.height = height;
        }
    }
}