package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parent;
    private String activeSelectedMod = "";

    private double scrollAmount = 0;
    private final int rowHeight = 22;

    // ── Action bar geometry ──────────────────────────────────────────────────
    // Matches the horizontal inset of the mod list rows: left+10, right-15.
    private static final int ROW_INSET_LEFT  = 10;
    private static final int ROW_INSET_RIGHT = 15;

    private static final int BAR_MARGIN      = 4;   // gap above separator and around buttons
    private static final int BTN_H           = 20;  // height matches vanilla MC buttons
    private static final int BTN_GAP         = 3;   // gap between the three buttons
    private static final int ACTION_BAR_HEIGHT = BAR_MARGIN + 1 + BAR_MARGIN + BTN_H + BAR_MARGIN;

    // Vanilla MC button sprites
    private static final Identifier SPR_BTN     = Identifier.withDefaultNamespace("gui/sprites/widget/button");
    private static final Identifier SPR_BTN_HOV = Identifier.withDefaultNamespace("gui/sprites/widget/button_highlighted");
    private static final Identifier SPR_BTN_DIS = Identifier.withDefaultNamespace("gui/sprites/widget/button_disabled");

    private static final int CLR_SEPARATOR = 0xFF555555;

    // Icon colours
    private static final int ICO_SAVE    = 0xFF55FF55;
    private static final int ICO_RESET   = 0xFF5599FF;
    private static final int ICO_DISCARD = 0xFFFF5555;
    private static final int ICO_DIS     = 0xFF666666;

    public ModListWidget(DLibMainScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
    }

    // ── Geometry helpers ─────────────────────────────────────────────────────

    /** Bottom of the scrollable list area (= where the action bar begins). */
    private int listBottom() {
        return this.getY() + this.height - ACTION_BAR_HEIGHT;
    }

    /**
     * Left x, right x, and width for the button strip.
     * Aligns with the mod-row hit area: getX()+ROW_INSET_LEFT … getX()+width-ROW_INSET_RIGHT.
     */
    private int btnAreaLeft()  { return this.getX() + ROW_INSET_LEFT; }
    private int btnAreaRight() { return this.getX() + this.width - ROW_INSET_RIGHT; }
    private int btnAreaWidth() { return btnAreaRight() - btnAreaLeft(); }

    /** Width of each of the three buttons given the total available area. */
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
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - scrollY * 14, totalH - viewHeight));
            return true;
        }
        return false;
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        drawModList(g, mx, my);
        drawActionBar(g, mx, my);
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

    private void drawActionBar(GuiGraphicsExtractor g, int mx, int my) {
        boolean active = parent.hasModSelected();

        // Separator line aligned to the button area edges
        int sepY = listBottom() + BAR_MARGIN;
        g.fill(btnAreaLeft(), sepY, btnAreaRight(), sepY + 1, CLR_SEPARATOR);

        // Three vanilla-sprite buttons
        int bY  = btnY();
        int bW  = btnW();

        for (int i = 0; i < 3; i++) {
            int bX  = btnX(i);
            boolean hov = active && mx >= bX && mx < bX + bW && my >= bY && my < bY + BTN_H;
            Identifier spr = active ? (hov ? SPR_BTN_HOV : SPR_BTN) : SPR_BTN_DIS;
            RenderUtil.drawSprite(g, RenderPipelines.GUI, spr, bX, bY, bW, BTN_H);
        }

        // Pixel icons centred in each button
        int icY = bY + BTN_H / 2;
        drawIconSave   (g, btnX(0) + bW / 2, icY, active ? ICO_SAVE    : ICO_DIS);
        drawIconReset  (g, btnX(1) + bW / 2, icY, active ? ICO_RESET   : ICO_DIS);
        drawIconDiscard(g, btnX(2) + bW / 2, icY, active ? ICO_DISCARD : ICO_DIS);
    }

    // ── Pixel-art icons (7×7 grid, all drawn with fill) ──────────────────────

    /**
     * ✔ checkmark — 2px stroke, 7 wide × 5 tall, centred on (cx, cy)
     *
     *   . . . . . █ .
     *   . . . . █ █ .
     *   █ . . █ █ . .
     *   █ █ █ █ . . .
     *   . █ █ . . . .
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
     * ✖ X — two crossing diagonals, 2px stroke, 7×7 grid centred on (cx, cy)
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
     * ⟲ reset arrow — circular arc with arrowhead, 7×7, centred on (cx, cy)
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
        // Arrowhead at open end (bottom-right)
        px(g, x+5, y+5, c);
        px(g, x+4, y+4, c); px(g, x+5, y+4, c); px(g, x+6, y+4, c);
    }

    private static void px(GuiGraphicsExtractor g, int x, int y, int c) {
        g.fill(x, y, x+1, y+1, c);
    }

    private static boolean isOver(int mx, int my, int x0, int y0, int x1, int y1) {
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        double mx = event.x(), my = event.y();

        // Action bar
        int bY = btnY(), bW = btnW();
        if (my >= bY && my < bY + BTN_H) {
            if (!parent.hasModSelected()) return true;
            for (int i = 0; i < 3; i++) {
                int bX = btnX(i);
                if (mx >= bX && mx < bX + bW) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    if (i == 0) parent.onSave();
                    else if (i == 1) parent.onResetDefaults();
                    else parent.onDiscard();
                    return true;
                }
            }
        }

        // Scrollbar
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

        // Mod rows
        for (int i = 0; i < mods.size(); i++) {
            int rowTopY = viewTop + (i * rowHeight) - (int) scrollAmount;
            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;
            if (mx >= this.getX() + ROW_INSET_LEFT && mx < this.getX() + this.width - ROW_INSET_RIGHT
                    && my >= rowTopY && my < rowTopY + rowHeight) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.activeSelectedMod = mods.get(i);
                this.parent.setSelectedMod(this.activeSelectedMod);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput n) {}
}