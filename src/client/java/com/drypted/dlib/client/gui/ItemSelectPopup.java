package com.drypted.dlib.client.gui;

import com.drypted.dlib.client.config.ConfigManager;
import com.drypted.dlib.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * A popup screen for selecting items from a list.
 * Supports single-select and multi-select modes.
 *
 * Selected items are stored as a comma-separated string of item identifiers
 * in ConfigOption.value (e.g. "minecraft:diamond,minecraft:iron_ingot").
 */
public class ItemSelectPopup extends Screen {

    private enum FilterMode { ALL, SELECTED }

    // Grid layout
    private static final int ITEM_TARGET_CELL_W = 34;
    private static final int ITEM_ROW_H = 34;
    private static final int ITEM_ICON_SIZE = 16;
    private static final int MIN_COLS = 4;
    private static final int MAX_COLS = 9;

    // Panel sizing
    private static final int MIN_PANEL_W = 260;
    private static final int MAX_PANEL_W = 460;
    private static final int MIN_PANEL_H = 240;
    private static final int MAX_PANEL_H = 420;
    private static final int PANEL_MARGIN = 18;

    private static final int PADDING = 10;

    // Footer buttons
    private static final int TAB_BTN_W = 56;
    private static final int TAB_BTN_H = 20;
    private static final int ACTION_BTN_W = 60;
    private static final int ACTION_BTN_H = 20;
    private static final int ACTION_BTN_GAP = 4;

    // ── Palette — matches ConfigListWidget / ModListWidget ─────────────────────
    private static final int COLOR_BACKDROP = 0xAA000000;  // Semi-transparent black overlay

    private static final int PANEL_BG          = 0xDD1A1A1A;  // Semi-transparent dark panel
    private static final int BORDER_LIGHT      = 0xFFAAAAAA;
    private static final int BORDER_DARK       = 0xFF666666;
    private static final int SEPARATOR         = 0xFF555555;

    private static final int TEXT_TITLE        = 0xFFFFFFFF;
    private static final int TEXT_LABEL        = 0xFFBBBBBB;
    private static final int TEXT_EMPTY        = 0xFF777777;

    private static final int ROW_HOVER         = 0x22FFFFFF;
    private static final int ROW_SELECTED      = 0x38FFFFFF;

    private static final int SCROLLBAR_TRACK      = 0x22FFFFFF;
    private static final int SCROLLBAR_THUMB      = 0x88FFFFFF;
    private static final int SCROLLBAR_THUMB_HOVER = 0xAAFFFFFF;

    private static final int ICO_SAVE    = 0xFF55FF55;
    private static final int ICO_DISCARD = 0xFFFF5555;

    private static final int TAB_TEXT_ACTIVE   = 0xFFFFFFFF;
    private static final int TAB_TEXT_INACTIVE = 0xFF999999;

    private final Screen parent;
    private final ConfigManager.ConfigOption option;
    private final boolean multi;
    private final List<String> availableItemIds;
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private final String originalValue;
    private final Runnable onClosed;

    // UI components
    private EditBox searchField;
    private String searchFilter = "";
    private FilterMode filterMode = FilterMode.ALL;
    private List<ItemStack> filteredItems = new ArrayList<>();
    private Map<ItemStack, String> itemStackToId = new HashMap<>();
    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    // Panel bounds (computed each init/resize)
    private int panelX, panelY, panelW, panelH;

    // Header bounds
    private int searchX, searchY, searchW, searchH;

    // Grid bounds (derived from panel bounds)
    private int gridStartX, gridEndX, gridTop, gridBottom, gridViewHeight;
    private int itemsPerRow;
    private float cellW;

    // Footer bounds / widgets
    private int footerRowY;
    private Button allTabButton, selectedTabButton;
    private Button saveButton, discardButton;

    // Scrollbar interaction
    private boolean draggingScrollbar = false;
    private int hoveredIndex = -1;

    public ItemSelectPopup(Screen parent, ConfigManager.ConfigOption option,
                           boolean multi, List<String> availableItemIds,
                           Runnable onClosed) {
        super(Component.literal("Select Item" + (multi ? "s" : "")));
        this.parent = parent;
        this.option = option;
        this.multi = multi;
        this.availableItemIds = availableItemIds;
        this.originalValue = option.value;
        this.onClosed = onClosed;

        // Parse existing selection
        if (option.value != null && !option.value.isEmpty()) {
            for (String id : option.value.split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    selectedIds.add(trimmed);
                }
            }
        }
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();

        // --- Panel bounds, responsive to screen size with equal margins ---
        panelW = Math.min(MAX_PANEL_W, (int)(this.width * 0.85f));
        panelH = Math.min(MAX_PANEL_H, (int)(this.height * 0.85f));

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // --- Header: title, "Search" label, search box ---
        int titleY = panelY + 8;
        int labelY = titleY + 16;
        searchY = labelY + 10;
        searchH = 18;
        searchX = panelX + PADDING;
        searchW = panelW - PADDING * 2;

        int header1Y = searchY + searchH + 8;

        // --- Footer: separator, filter tabs (left), save/discard (right) ---
        int footerBottom = panelY + panelH - PADDING;
        footerRowY = footerBottom - ACTION_BTN_H;
        int header2Y = footerRowY - 8;

        // --- Grid: fills between the two dividers, edges match the search box ---
        gridTop = header1Y + 6;
        gridBottom = header2Y - 6;
        gridViewHeight = gridBottom - gridTop;

        gridStartX = searchX;
        gridEndX = searchX + searchW;
        int usableGridW = gridEndX - gridStartX;
        itemsPerRow = Mth.clamp(usableGridW / ITEM_TARGET_CELL_W, MIN_COLS, MAX_COLS);
        cellW = usableGridW / (float) itemsPerRow;

        // Search field - plain vanilla EditBox
        this.searchField = new EditBox(mc.font, searchX, searchY, searchW, searchH, Component.empty());
        this.searchField.setValue(this.searchFilter);
        this.searchField.setTextColor(0xFFFFFFFF);
        this.searchField.setTextColorUneditable(0xFF999999);
        this.searchField.setHint(Component.literal("Type to search..."));
        this.searchField.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchField);

        // Filter tabs (bottom-left)
        this.allTabButton = Button.builder(tabLabel("All", filterMode == FilterMode.ALL), b -> setFilterMode(FilterMode.ALL))
                .bounds(panelX + PADDING, footerRowY, TAB_BTN_W, TAB_BTN_H).build();
        this.selectedTabButton = Button.builder(tabLabel("Selected", filterMode == FilterMode.SELECTED), b -> setFilterMode(FilterMode.SELECTED))
                .bounds(panelX + PADDING + TAB_BTN_W + 4, footerRowY, TAB_BTN_W, TAB_BTN_H).build();
        this.addRenderableWidget(this.allTabButton);
        this.addRenderableWidget(this.selectedTabButton);

        // Save / Discard (bottom-right)
        int saveX = panelX + panelW - PADDING - ACTION_BTN_W;
        int discardX = saveX - ACTION_BTN_GAP - ACTION_BTN_W;
        this.discardButton = Button.builder(Component.literal(""), b -> onDiscard())
                .bounds(discardX, footerRowY, ACTION_BTN_W, ACTION_BTN_H).build();
        this.saveButton = Button.builder(Component.literal(""), b -> onDone())
                .bounds(saveX, footerRowY, ACTION_BTN_W, ACTION_BTN_H).build();
        this.addRenderableWidget(this.discardButton);
        this.addRenderableWidget(this.saveButton);

        rebuildFilteredList();

        // Clamp scroll in case the resize shrank the content
        int maxScroll = Math.max(0, totalContentHeight - gridViewHeight);
        this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, maxScroll));

        this.setInitialFocus(this.searchField);
    }

    private Component tabLabel(String text, boolean active) {
        return Component.literal(text).withStyle(s -> s.withColor(active ? TAB_TEXT_ACTIVE : TAB_TEXT_INACTIVE));
    }

    private void setFilterMode(FilterMode mode) {
        if (this.filterMode == mode) return;
        this.filterMode = mode;
        this.allTabButton.setMessage(tabLabel("All", mode == FilterMode.ALL));
        this.selectedTabButton.setMessage(tabLabel("Selected", mode == FilterMode.SELECTED));
        this.scrollAmount = 0;
        rebuildFilteredList();
    }

    private void onSearchChanged(String text) {
        this.searchFilter = text.toLowerCase(Locale.ROOT).trim();
        this.scrollAmount = 0;
        rebuildFilteredList();
    }

    private void rebuildFilteredList() {
        filteredItems.clear();
        itemStackToId.clear();
        for (String itemId : availableItemIds) {
            if (filterMode == FilterMode.SELECTED && !selectedIds.contains(itemId)) continue;
            if (!searchFilter.isEmpty() && !itemId.toLowerCase(Locale.ROOT).contains(searchFilter)) continue;

            ItemStack stack = resolveItemStack(itemId);
            if (stack != null && !stack.isEmpty()) {
                filteredItems.add(stack);
                itemStackToId.put(stack, itemId);
            }
        }

        int rows = (int) Math.ceil((double) filteredItems.size() / itemsPerRow);
        totalContentHeight = rows * ITEM_ROW_H + PADDING;
    }

    private ItemStack resolveItemStack(String itemId) {
        try {
            int colonIdx = itemId.indexOf(':');
            Identifier id;
            if (colonIdx >= 0) {
                id = Identifier.fromNamespaceAndPath(itemId.substring(0, colonIdx), itemId.substring(colonIdx + 1));
            } else {
                id = Identifier.fromNamespaceAndPath("minecraft", itemId);
            }
            Item item = BuiltInRegistries.ITEM.get(id).map(Holder.Reference::value).orElse(null);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // 1. Draw backdrop and panel FIRST (behind everything)
        drawBackdropAndPanel(graphics);

        // 2. Draw custom grid content
        drawGridContent(graphics, mouseX, mouseY);

        // 3. Draw the widgets (buttons, search field) ON TOP
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // 4. Draw icon overlays on top of buttons
        drawIconOverlays(graphics);
    }

    private void drawBackdropAndPanel(GuiGraphicsExtractor graphics) {
        // Semi-transparent black overlay on the game world
        // graphics.fill(0, 0, this.width, this.height, COLOR_BACKDROP);

        // Panel background - semi-transparent dark
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, COLOR_BACKDROP);

        // Panel border - light on top and left, dark on bottom and right
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 1, BORDER_LIGHT);                 // top
        graphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, BORDER_DARK); // bottom
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelH, BORDER_LIGHT);                 // left
        graphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, BORDER_DARK); // right

        // Corner fixes (dark corners)
        graphics.fill(panelX, panelY, panelX + 1, panelY + 1, BORDER_DARK);
        graphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + 1, BORDER_DARK);
        graphics.fill(panelX, panelY + panelH - 1, panelX + 1, panelY + panelH, BORDER_DARK);
        graphics.fill(panelX + panelW - 1, panelY + panelH - 1, panelX + panelW, panelY + panelH, BORDER_DARK);

        // Title (left-aligned, matches "Mod Config: X" header convention)
        String title = multi ? "Select Items" : "Select Item";
        RenderUtil.drawScaledText(graphics, title, 1.2f, panelX + PADDING, panelY + 8, TEXT_TITLE, 1.0f, true);

        // "Search" label, left-aligned
        RenderUtil.drawScaledText(graphics, "Search", 0.85f, searchX, searchY - 10, TEXT_LABEL, 1.0f, false);

        // Header divider
        int header1Y = searchY + searchH + 8;
        graphics.fill(panelX + PADDING, header1Y, panelX + panelW - PADDING, header1Y + 1, SEPARATOR);

        // Footer divider
        int header2Y = footerRowY - 8;
        graphics.fill(panelX + PADDING, header2Y, panelX + panelW - PADDING, header2Y + 1, SEPARATOR);
    }

    private void drawGridContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // --- Item grid ---
        graphics.enableScissor(panelX, gridTop, panelX + panelW, gridBottom);

        hoveredIndex = -1;
        if (filteredItems.isEmpty()) {
            String empty;
            if (filterMode == FilterMode.SELECTED) {
                empty = "No items selected";
            } else {
                empty = searchFilter.isEmpty() ? "No items available" : "No items match \"" + searchFilter + "\"";
            }
            int ew = Minecraft.getInstance().font.width(empty);
            RenderUtil.drawScaledText(graphics, empty, 1.0f,
                    panelX + panelW / 2 - ew / 2, gridTop + gridViewHeight / 2 - 4,
                    TEXT_EMPTY, 1.0f, false);
        } else {
            for (int i = 0; i < filteredItems.size(); i++) {
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;
                int cellX = gridStartX + Math.round(col * cellW);
                int cellRight = gridStartX + Math.round((col + 1) * cellW);
                int cellY = gridTop + row * ITEM_ROW_H - (int) scrollAmount;
                int cw = cellRight - cellX;

                if (cellY + ITEM_ROW_H < gridTop || cellY > gridBottom) continue;

                ItemStack stack = filteredItems.get(i);
                String itemId = getItemId(stack);
                boolean isSelected = selectedIds.contains(itemId);
                boolean isHovered = mouseX >= cellX && mouseX < cellRight &&
                        mouseY >= cellY && mouseY < cellY + ITEM_ROW_H &&
                        mouseY >= gridTop && mouseY < gridBottom;

                if (isHovered) hoveredIndex = i;

                // Translucent overlay for hover/selected
                if (isSelected) {
                    graphics.fill(cellX + 1, cellY + 1, cellRight - 1, cellY + ITEM_ROW_H - 1, ROW_SELECTED);
                } else if (isHovered) {
                    graphics.fill(cellX + 1, cellY + 1, cellRight - 1, cellY + ITEM_ROW_H - 1, ROW_HOVER);
                }

                // Item icon (centered)
                int iconX = cellX + (cw - ITEM_ICON_SIZE) / 2;
                int iconY = cellY + (ITEM_ROW_H - ITEM_ICON_SIZE) / 2;
                RenderUtil.drawItemIcon(graphics, stack, iconX, iconY, ITEM_ICON_SIZE, ITEM_ICON_SIZE);

                // Small selected badge (top-right corner)
                if (isSelected) {
                    drawIconSave(graphics, cellRight - 8, cellY + 8, ICO_SAVE);
                }
            }
        }

        graphics.disableScissor();

        // Hover tooltip - format: "Name - minecraft:id"
        if (hoveredIndex >= 0) {
            ItemStack hoveredStack = filteredItems.get(hoveredIndex);
            String itemId = getItemId(hoveredStack);
            boolean isSelected = selectedIds.contains(itemId);
            
            String displayName = getDisplayName(itemId);
            String tooltipText = displayName + " - " + itemId;
            
            // Add checkmark if selected
            if (isSelected) {
                tooltipText = tooltipText + " ✓";
            }
            
            Component tooltip = Component.literal(tooltipText)
                    .withStyle(s -> s.withColor(isSelected ? ICO_SAVE : 0xFFFFFFFF));
            graphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }

        // Scrollbar — thin, matches ConfigListWidget's track/thumb style
        if (totalContentHeight > gridViewHeight) {
            int scrollX = gridEndX - 4;
            int thumbH = Math.max(12, (gridViewHeight * gridViewHeight) / totalContentHeight);
            int maxScroll = Math.max(0, totalContentHeight - gridViewHeight);
            int thumbY = gridTop + (int) ((scrollAmount / Math.max(1, maxScroll)) * (gridViewHeight - thumbH));
            boolean thumbHovered = mouseX >= scrollX - 1 && mouseX <= scrollX + 5 && mouseY >= thumbY && mouseY <= thumbY + thumbH;

            graphics.fill(scrollX, gridTop, scrollX + 3, gridBottom, SCROLLBAR_TRACK);
            graphics.fill(scrollX, thumbY, scrollX + 3, thumbY + thumbH,
                    (thumbHovered || draggingScrollbar) ? SCROLLBAR_THUMB_HOVER : SCROLLBAR_THUMB);
        }
    }

    private void drawIconOverlays(GuiGraphicsExtractor graphics) {
        // Icon overlays on the Save/Discard buttons
        drawIconDiscard(graphics, discardButton.getX() + ACTION_BTN_W / 2, footerRowY + ACTION_BTN_H / 2, ICO_DISCARD);
        drawIconSave(graphics, saveButton.getX() + ACTION_BTN_W / 2, footerRowY + ACTION_BTN_H / 2, ICO_SAVE);
    }

    // ── Pixel-art icons ────────────────────────────────────────────────────────

    /** Checkmark, 7 wide × 5 tall, centred on (cx, cy). */
    private static void drawIconSave(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3, y = cy - 2;
        px(g, x + 5, y, c);
        px(g, x + 4, y + 1, c); px(g, x + 5, y + 1, c);
        px(g, x, y + 2, c); px(g, x + 3, y + 2, c); px(g, x + 4, y + 2, c);
        px(g, x, y + 3, c); px(g, x + 1, y + 3, c); px(g, x + 2, y + 3, c); px(g, x + 3, y + 3, c);
        px(g, x + 1, y + 4, c); px(g, x + 2, y + 4, c);
    }

    /** X, two crossing diagonals, 7×7, centred on (cx, cy). */
    private static void drawIconDiscard(GuiGraphicsExtractor g, int cx, int cy, int c) {
        int x = cx - 3, y = cy - 3;
        for (int i = 0; i < 7; i++) {
            px(g, x + i, y + i, c);
            px(g, x + i + 1, y + i, c);
            px(g, x + 6 - i, y + i, c);
            px(g, x + 5 - i, y + i, c);
        }
    }

    private static void px(GuiGraphicsExtractor g, int x, int y, int c) {
        g.fill(x, y, x + 1, y + 1, c);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        double mx = event.x(), my = event.y();

        // Scrollbar click/grab
        if (totalContentHeight > gridViewHeight) {
            int scrollX = gridEndX - 4;
            if (mx >= scrollX - 2 && mx <= scrollX + 6 && my >= gridTop && my <= gridBottom) {
                draggingScrollbar = true;
                updateScrollFromMouseY(my);
                return true;
            }
        }

        // Item grid clicks
        if (my >= gridTop && my <= gridBottom) {
            for (int i = 0; i < filteredItems.size(); i++) {
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;
                int cellX = gridStartX + Math.round(col * cellW);
                int cellRight = gridStartX + Math.round((col + 1) * cellW);
                int cellY = gridTop + row * ITEM_ROW_H - (int) scrollAmount;

                if (cellY + ITEM_ROW_H < gridTop || cellY > gridBottom) continue;

                if (mx >= cellX && mx < cellRight &&
                        my >= cellY && my < cellY + ITEM_ROW_H) {

                    ItemStack stack = filteredItems.get(i);
                    String itemId = getItemId(stack);

                    if (multi) {
                        if (selectedIds.contains(itemId)) {
                            selectedIds.remove(itemId);
                        } else {
                            selectedIds.add(itemId);
                        }
                    } else {
                        selectedIds.clear();
                        selectedIds.add(itemId);
                    }

                    if (filterMode == FilterMode.SELECTED) {
                        rebuildFilteredList();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouseY(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updateScrollFromMouseY(double my) {
        double pct = (my - gridTop) / (double) gridViewHeight;
        int maxScroll = Math.max(0, totalContentHeight - gridViewHeight);
        this.scrollAmount = Math.max(0, Math.min(pct * maxScroll, maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (totalContentHeight > gridViewHeight) {
            int maxScroll = Math.max(0, totalContentHeight - gridViewHeight);
            this.scrollAmount = Math.max(0, Math.min(scrollAmount - scrollY * 20, maxScroll));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.isEscape()) {
            onDiscard();
            return true;
        }
        return super.keyPressed(event);
    }

    private void onDone() {
        // Save selection to config option
        if (selectedIds.isEmpty()) {
            option.value = "";
        } else {
            option.value = String.join(",", selectedIds);
        }
        if (onClosed != null) onClosed.run();
        if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
    }

    private void onDiscard() {
        // Revert to original value
        option.value = originalValue;
        if (onClosed != null) onClosed.run();
        if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void onClose() {
        onDiscard();
    }

    // --- Helpers ---

    private String getItemId(ItemStack stack) {
        return itemStackToId.getOrDefault(stack, "unknown");
    }

    private String getDisplayName(String fullId) {
        int colonIdx = fullId.indexOf(':');
        String name = colonIdx >= 0 ? fullId.substring(colonIdx + 1) : fullId;
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name.replace('_', ' ');
    }

    /**
     * Returns the selected item IDs as a comma-separated string.
     */
    public String getSelectedValue() {
        return String.join(",", selectedIds);
    }
}