package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModListWidget extends AbstractWidget {
    private final DLibMainScreen parent;
    private String activeSelectedMod = "";

    private double scrollAmount = 0;
    private final int rowHeight = 22;

    // ── Action bar ───────────────────────────────────────────────────────────
    // 4px margin above the separator line, then 3 buttons of BTN_H height,
    // then 4px margin below — total strip height accounts for all of that.
    private static final int BAR_MARGIN   = 4;  // gap between list and separator
    private static final int BTN_H        = 18; // button height (matches MC default)
    private static final int BTN_PADDING  = 3;  // horizontal gap between buttons
    private static final int ACTION_BAR_HEIGHT = BAR_MARGIN + 1 + BAR_MARGIN + BTN_H + BAR_MARGIN;
    // breakdown: margin | separator | margin | button | bottom margin

    // Minecraft-style button colours (matches vanilla button palette)
    private static final int MC_BTN_FACE        = 0xFF5A5A5A; // normal face
    private static final int MC_BTN_FACE_HOV    = 0xFF7A7A7A; // hovered face
    private static final int MC_BTN_FACE_DIS    = 0xFF3A3A3A; // disabled face
    private static final int MC_BTN_LIGHT       = 0xFFBFBFBF; // top+left highlight
    private static final int MC_BTN_LIGHT_HOV   = 0xFFDFDFDF;
    private static final int MC_BTN_SHADOW      = 0xFF1A1A1A; // bottom+right shadow
    private static final int MC_BTN_SHADOW_DIS  = 0xFF2A2A2A;

    // Icon pixel colours (only used when active)
    private static final int ICO_SAVE    = 0xFF55FF55; // green  — checkmark
    private static final int ICO_RESET   = 0xFF5599FF; // blue   — reset arrows
    private static final int ICO_DISCARD = 0xFFFF5555; // red    — X
    private static final int ICO_DIS     = 0xFF555555; // greyed out

    // Separator
    private static final int CLR_SEPARATOR = 0xFF555555;

    public ModListWidget(DLibMainScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
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
            int maxScroll = totalH - viewHeight;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - scrollY * 14, maxScroll));
            return true;
        }
        return false;
    }

    /** Y coordinate where the mod list ends (= where the action-bar region begins). */
    private int listBottom() {
        return this.getY() + this.height - ACTION_BAR_HEIGHT;
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        drawModList(g, mx, my);
        drawActionBar(g, mx, my);
    }

    // -- Mod list -------------------------------------------------------------

    private void drawModList(GuiGraphicsExtractor g, int mx, int my) {
        int viewTop    = this.getY() + 15;
        int viewBottom = listBottom();
        int viewHeight = viewBottom - viewTop;
        List<String> mods = ConfigManager.getRegisteredMods();

        for (int i = 0; i < mods.size(); i++) {
            String modId  = mods.get(i);
            int rowTopY   = viewTop + (i * rowHeight) - (int) scrollAmount;
            if (rowTopY + rowHeight < viewTop || rowTopY > viewBottom) continue;

            boolean hov = mx >= this.getX() + 10 && mx < this.getX() + this.width - 15
                       && my >= rowTopY && my < rowTopY + rowHeight;
            boolean sel = modId.equals(activeSelectedMod);

            if (sel)      g.fill(this.getX() + 10, rowTopY, this.getX() + this.width - 15, rowTopY + rowHeight - 2, 0x44FFFFFF);
            else if (hov) g.fill(this.getX() + 10, rowTopY, this.getX() + this.width - 15, rowTopY + rowHeight - 2, 0x11FFFFFF);

            int textColor = sel ? 0xFFFFCC00 : (hov ? 0xFFFFFFFF : 0xFF999999);
            RenderUtil.drawScaledText(g, ConfigManager.getModDisplayName(modId),
                    1.0f, this.getX() + 16, rowTopY + 5, textColor, 1.0f, true);
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

    // -- Action bar -----------------------------------------------------------

    private void drawActionBar(GuiGraphicsExtractor g, int mx, int my) {
        boolean active = parent.hasModSelected();

        // Separator line with margin above it
        int sepY = listBottom() + BAR_MARGIN;
        g.fill(this.getX() + BTN_PADDING, sepY,
               this.getX() + this.width - BTN_PADDING, sepY + 1, CLR_SEPARATOR);

        // Three buttons below the separator
        int btnY   = sepY + 1 + BAR_MARGIN;
        int totalW = this.width - BTN_PADDING * 4; // 4 gaps: left|btn|gap|btn|gap|btn|right
        int btnW   = totalW / 3;

        int bx0 = this.getX() + BTN_PADDING;
        int bx1 = bx0 + btnW + BTN_PADDING;
        int bx2 = bx1 + btnW + BTN_PADDING;

        boolean h0 = active && isOver(mx, my, bx0, btnY, bx0 + btnW, btnY + BTN_H);
        boolean h1 = active && isOver(mx, my, bx1, btnY, bx1 + btnW, btnY + BTN_H);
        boolean h2 = active && isOver(mx, my, bx2, btnY, bx2 + btnW, btnY + BTN_H);

        drawMcButton(g, bx0, btnY, btnW, BTN_H, active, h0);
        drawMcButton(g, bx1, btnY, btnW, BTN_H, active, h1);
        drawMcButton(g, bx2, btnY, btnW, BTN_H, active, h2);

        // Pixel icons centred in each button
        int iconCy = btnY + BTN_H / 2; // vertical centre of button
        drawIconSave   (g, bx0 + btnW / 2, iconCy, active ? ICO_SAVE    : ICO_DIS);
        drawIconReset  (g, bx1 + btnW / 2, iconCy, active ? ICO_RESET   : ICO_DIS);
        drawIconDiscard(g, bx2 + btnW / 2, iconCy, active ? ICO_DISCARD : ICO_DIS);
    }

    /**
     * Draws a Minecraft-style bevelled button:
     *   – 1px light top + left edge
     *   – 1px dark bottom + right edge
     *   – solid face fill
     */
    private void drawMcButton(GuiGraphicsExtractor g, int x, int y, int w, int h,
                               boolean active, boolean hovered) {
        int face   = active ? (hovered ? MC_BTN_FACE_HOV : MC_BTN_FACE) : MC_BTN_FACE_DIS;
        int light  = active ? (hovered ? MC_BTN_LIGHT_HOV : MC_BTN_LIGHT) : MC_BTN_SHADOW_DIS;
        int shadow = MC_BTN_SHADOW;

        // Face
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, face);
        // Top edge
        g.fill(x,         y,         x + w,     y + 1,     light);
        // Left edge
        g.fill(x,         y + 1,     x + 1,     y + h - 1, light);
        // Bottom edge
        g.fill(x,         y + h - 1, x + w,     y + h,     shadow);
        // Right edge
        g.fill(x + w - 1, y + 1,     x + w,     y + h - 1, shadow);
    }

    // ── Pixel-art icons ──────────────────────────────────────────────────────
    // All icons are drawn on a 7×7 grid centred on (cx, cy).
    // Each uses only graphics.fill so they are pixel-perfect and identical in
    // visual weight regardless of the font atlas.

    /**
     * ✔ checkmark on a 7×6 grid, 2px stroke weight.
     * Left arm drops down-right, right arm climbs up-right.
     *
     *   . . . . . X .
     *   . . . . X X .
     *   X . . X X . .
     *   X X X X . . .
     *   . X X . . . .
     */
    private void drawIconSave(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3;
        int y = cy - 2; // top of the 5-row icon
        // Row 0
        px(g, x + 5, y,     c);
        // Row 1
        px(g, x + 4, y + 1, c); px(g, x + 5, y + 1, c);
        // Row 2
        px(g, x,     y + 2, c); px(g, x + 3, y + 2, c); px(g, x + 4, y + 2, c);
        // Row 3
        px(g, x,     y + 3, c); px(g, x + 1, y + 3, c); px(g, x + 2, y + 3, c); px(g, x + 3, y + 3, c);
        // Row 4
        px(g, x + 1, y + 4, c); px(g, x + 2, y + 4, c);
    }

    /** ✖ X — two diagonal strokes, each 2px thick, on a 7×7 grid */
    private void drawIconDiscard(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3;
        int y = cy - 3;
        for (int i = 0; i < 7; i++) {
            px(g, x + i,     y + i,     c); // top-left → bottom-right
            px(g, x + i + 1, y + i,     c); // 2px wide
            px(g, x + 6 - i, y + i,     c); // top-right → bottom-left
            px(g, x + 5 - i, y + i,     c); // 2px wide
        }
    }

    /**
     * ⟲ reset — clockwise circular arrow on a 7×7 grid.
     * Drawn as an arc of 5/8 of a circle with a small arrowhead.
     */
    private void drawIconReset(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3;
        int y = cy - 3;
        // Arc pixels (hand-tuned for a 7×7 circle minus bottom-right gap)
        // Top row
        px(g, x + 2, y,     c); px(g, x + 3, y,     c); px(g, x + 4, y,     c);
        // Upper corners
        px(g, x + 1, y + 1, c); px(g, x + 5, y + 1, c);
        // Sides
        px(g, x,     y + 2, c); px(g, x + 6, y + 2, c);
        px(g, x,     y + 3, c); px(g, x + 6, y + 3, c);
        px(g, x,     y + 4, c);
        // Bottom-left corner
        px(g, x + 1, y + 5, c); px(g, x + 2, y + 6, c);
        // Bottom partial (arc ends ~bottom-centre)
        px(g, x + 3, y + 6, c);
        // Arrowhead pointing right at the open end (bottom-right)
        // The tail of the arc ends near (x+4, y+6); arrow points downward-right
        px(g, x + 5, y + 5, c); // shaft
        px(g, x + 4, y + 4, c); // inner arrowhead
        px(g, x + 6, y + 4, c); // outer arrowhead
        px(g, x + 5, y + 4, c); // fill
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Draw a single 1×1 pixel. */
    private static void px(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + 1, y + 1, color);
    }

    private static boolean isOver(int mx, int my, int x0, int y0, int x1, int y1) {
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        // Action bar
        int sepY = listBottom() + BAR_MARGIN;
        int btnY = sepY + 1 + BAR_MARGIN;
        int btnBottom = btnY + BTN_H;
        if (my >= btnY && my < btnBottom && mx >= this.getX() && mx < this.getX() + this.width) {
            if (!parent.hasModSelected()) return true;
            this.playDownSound(Minecraft.getInstance().getSoundManager());

            int totalW = this.width - BTN_PADDING * 4;
            int btnW   = totalW / 3;
            int bx0 = this.getX() + BTN_PADDING;
            int bx1 = bx0 + btnW + BTN_PADDING;
            int bx2 = bx1 + btnW + BTN_PADDING;

            if (mx >= bx0 && mx < bx0 + btnW)      parent.onSave();
            else if (mx >= bx1 && mx < bx1 + btnW) parent.onResetDefaults();
            else if (mx >= bx2 && mx < bx2 + btnW) parent.onDiscard();
            return true;
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
            if (mx >= this.getX() + 10 && mx < this.getX() + this.width - 15
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