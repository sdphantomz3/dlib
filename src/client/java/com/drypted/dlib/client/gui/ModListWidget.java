package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parent;
    private String activeSelectedMod = "";

    private double scrollAmount = 0;
    private final int rowHeight = 22;

    // ── Action bar geometry ──────────────────────────────────────────────────
    private static final int ROW_INSET_LEFT  = 10;
    private static final int ROW_INSET_RIGHT = 15;

    private static final int BAR_MARGIN      = 4;
    private static final int BTN_H           = 20;
    private static final int BTN_GAP         = 3;
    private static final int ACTION_BAR_HEIGHT = BAR_MARGIN + 1 + BAR_MARGIN + BTN_H + BAR_MARGIN;

    private static final int CLR_SEPARATOR = 0xFF555555;

    // Icon colours (same as original)
    private static final int ICO_SAVE    = 0xFF55FF55;
    private static final int ICO_RESET   = 0xFF5599FF;
    private static final int ICO_DISCARD = 0xFFFF5555;
    private static final int ICO_DIS     = 0xFF666666;

    
    public void setSelectedMod(String modId) {
        this.activeSelectedMod = modId;
        List<String> mods = ConfigManager.getRegisteredMods();
        int index = mods.indexOf(modId);
        if (index != -1) {
            int viewTop = this.getY() + 15;
            int viewBottom = listBottom();
            int viewHeight = viewBottom - viewTop;
            int targetY = index * rowHeight;
            // Clamp scroll to show the row
            if (targetY < scrollAmount) {
                scrollAmount = targetY;
            } else if (targetY + rowHeight > scrollAmount + viewHeight) {
                scrollAmount = targetY + rowHeight - viewHeight;
            }
        }
    }

    // ── Three action buttons ─────────────────────────────────────────────────
    private final Button saveButton;
    private final Button resetButton;
    private final Button discardButton;

    public ModListWidget(DLibMainScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;

        saveButton = Button.builder(
                Component.literal(""),
                btn -> parent.onSave()
        ).bounds(0, 0, 0, 0).build();

        resetButton = Button.builder(
                Component.literal(""),
                btn -> parent.onResetDefaults()
        ).bounds(0, 0, 0, 0).build();

        discardButton = Button.builder(
                Component.literal(""),
                btn -> parent.onDiscard()
        ).bounds(0, 0, 0, 0).build();
    }

    // ── Geometry helpers ─────────────────────────────────────────────────────

    private int listBottom() {
        return this.getY() + this.height - ACTION_BAR_HEIGHT;
    }

    private int btnAreaLeft()  { return this.getX() + ROW_INSET_LEFT; }
    private int btnAreaRight() { return this.getX() + this.width - ROW_INSET_RIGHT; }
    private int btnAreaWidth() { return btnAreaRight() - btnAreaLeft(); }

    private int btnW() {
        return (btnAreaWidth() - BTN_GAP * 2) / 3;
    }

    private int btnX(int idx) {
        return btnAreaLeft() + idx * (btnW() + BTN_GAP);
    }

    private int btnY() {
        return listBottom() + BAR_MARGIN + 1 + BAR_MARGIN;
    }

    // ── Scroll ───────────────────────────────────────────────────────────────

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int viewTop    = this.getY() + 15;
        int viewBottom = listBottom();
        int viewHeight = viewBottom - viewTop;
        int totalH     = ConfigManager.getRegisteredMods().size() * rowHeight;
        if (totalH <= viewHeight) return false;
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width
                && mouseY >= this.getY() && mouseY <= viewBottom) {
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount + scrollY * 14, totalH - viewHeight));
            return true;
        }
        return false;
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        drawModList(g, mx, my);
        drawActionBar(g, mx, my, pt);
    }

    private void drawModList(GuiGraphicsExtractor g, int mx, int my) {
        int viewTop    = this.getY() + 15;
        int viewBottom = listBottom();
        int viewHeight = viewBottom - viewTop;
        List<String> mods = ConfigManager.getRegisteredMods();

        for (int i = 0; i < mods.size(); i++) {
            String modId  = mods.get(i);
            int rowTopY   = viewTop + (i * rowHeight) - (int) scrollAmount;
            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;

            boolean hov = mx >= this.getX() + ROW_INSET_LEFT
                       && mx <  this.getX() + this.width - ROW_INSET_RIGHT
                       && my >= rowTopY && my < rowTopY + rowHeight;
            boolean sel = modId.equals(activeSelectedMod);

            if (sel)      g.fill(this.getX() + ROW_INSET_LEFT, rowTopY, this.getX() + this.width - ROW_INSET_RIGHT, rowTopY + rowHeight - 2, 0x44FFFFFF);
            else if (hov) g.fill(this.getX() + ROW_INSET_LEFT, rowTopY, this.getX() + this.width - ROW_INSET_RIGHT, rowTopY + rowHeight - 2, 0x11FFFFFF);

            int textColor = sel ? 0xFFFFCC00 : (hov ? 0xFFFFFFFF : 0xFF999999);
            RenderUtil.drawScaledText(g, ConfigManager.getModDisplayName(modId),
                    1.0f, this.getX() + ROW_INSET_LEFT + 6, rowTopY + 5, textColor, 1.0f, true);
        }

        int totalH = mods.size() * rowHeight;
        if (totalH > viewHeight) {
            int sx = this.getX() + this.width - 8;
            int th = Math.max(12, (viewHeight * viewHeight) / totalH);
            int ms = totalH - viewHeight;
            int ty = viewTop + (int) ((scrollAmount / ms) * (viewHeight - th));
            g.fill(sx, viewTop, sx + 3, viewBottom, 0x11FFFFFF);
            g.fill(sx, ty,      sx + 3, ty + th,    0x66FFFFFF);
        }
    }

    private void drawActionBar(GuiGraphicsExtractor g, int mx, int my, float pt) {
        boolean active = parent.hasModSelected();

        // Separator line
        int sepY = listBottom() + BAR_MARGIN;
        g.fill(btnAreaLeft(), sepY, btnAreaRight(), sepY + 1, CLR_SEPARATOR);

        // Update button positions and sizes
        int bW = btnW();
        int bY = btnY();

        // ── Save button ──
        int x1 = btnX(0);
        saveButton.setX(x1);
        saveButton.setY(bY);
        saveButton.setWidth(bW);
        saveButton.setHeight(BTN_H);
        saveButton.active = active;
        saveButton.extractRenderState(g, mx, my, pt);
        // Draw icon on top
        drawIconSave(g, x1 + bW / 2, bY + BTN_H / 2, active ? ICO_SAVE : ICO_DIS);

        // ── Reset button ──
        int x2 = btnX(1);
        resetButton.setX(x2);
        resetButton.setY(bY);
        resetButton.setWidth(bW);
        resetButton.setHeight(BTN_H);
        resetButton.active = active;
        resetButton.extractRenderState(g, mx, my, pt);
        drawIconReset(g, x2 + bW / 2, bY + BTN_H / 2, active ? ICO_RESET : ICO_DIS);

        // ── Discard button ──
        int x3 = btnX(2);
        discardButton.setX(x3);
        discardButton.setY(bY);
        discardButton.setWidth(bW);
        discardButton.setHeight(BTN_H);
        discardButton.active = active;
        discardButton.extractRenderState(g, mx, my, pt);
        drawIconDiscard(g, x3 + bW / 2, bY + BTN_H / 2, active ? ICO_DISCARD : ICO_DIS);
    }

    // ── Pixel-art icons (restored from original) ─────────────────────────────

    /**
     * ✔ checkmark – 7 wide × 5 tall, centred on (cx, cy)
     */
    private void drawIconSave(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3, y = cy - 2;
        px(g, x+5, y,   c);
        px(g, x+4, y+1, c); px(g, x+5, y+1, c);
        px(g, x,   y+2, c); px(g, x+3, y+2, c); px(g, x+4, y+2, c);
        px(g, x,   y+3, c); px(g, x+1, y+3, c); px(g, x+2, y+3, c); px(g, x+3, y+3, c);
        px(g, x+1, y+4, c); px(g, x+2, y+4, c);
    }

    /**
     * ✖ X – two crossing diagonals, 7×7, centred on (cx, cy)
     */
    private void drawIconDiscard(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3, y = cy - 3;
        for (int i = 0; i < 7; i++) {
            px(g, x+i,   y+i,   c);
            px(g, x+i+1, y+i,   c);
            px(g, x+6-i, y+i,   c);
            px(g, x+5-i, y+i,   c);
        }
    }

    /**
     * ⟲ reset arrow – circular arc with arrowhead, 7×7, centred on (cx, cy)
     */
    private void drawIconReset(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3, y = cy - 3;
        // Arc
        px(g, x+2, y,   c); px(g, x+3, y,   c); px(g, x+4, y,   c);
        px(g, x+1, y+1, c); px(g, x+5, y+1, c);
        px(g, x,   y+2, c); px(g, x+6, y+2, c);
        px(g, x,   y+3, c); px(g, x+6, y+3, c);
        px(g, x,   y+4, c);
        px(g, x+1, y+5, c); px(g, x+2, y+6, c); px(g, x+3, y+6, c);
        // Arrowhead
        px(g, x+5, y+5, c);
        px(g, x+4, y+4, c); px(g, x+5, y+4, c); px(g, x+6, y+4, c);
    }

    private static void px(GuiGraphicsExtractor g, int x, int y, int c) {
        g.fill(x, y, x+1, y+1, c);
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        double mx = event.x(), my = event.y();

        // Let the buttons handle clicks first
        if (saveButton.mouseClicked(event, doubleClick)) return true;
        if (resetButton.mouseClicked(event, doubleClick)) return true;
        if (discardButton.mouseClicked(event, doubleClick)) return true;

        // Scrollbar click
        int viewTop    = this.getY() + 15;
        int viewBottom = listBottom();
        int viewHeight = viewBottom - viewTop;
        List<String> mods = ConfigManager.getRegisteredMods();
        int totalH = mods.size() * rowHeight;

        if (totalH > viewHeight
                && mx >= this.getX() + this.width - 12 && mx <= this.getX() + this.width
                && my >= viewTop && my <= viewBottom) {
            double pct = (my - viewTop) / (double) viewHeight;
            this.scrollAmount = Math.max(0, Math.min(pct * (totalH - viewHeight), totalH - viewHeight));
            return true;
        }

        // Mod rows click
        for (int i = 0; i < mods.size(); i++) {
            int rowTopY = viewTop + (i * rowHeight) - (int) scrollAmount;
            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;
            if (mx >= this.getX() + ROW_INSET_LEFT && mx < this.getX() + this.width - ROW_INSET_RIGHT
                    && my >= rowTopY && my < rowTopY + rowHeight) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.activeSelectedMod = mods.get(i);
                this.parent.setSelectedMod(mods.get(i)); 
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput n) {}
}