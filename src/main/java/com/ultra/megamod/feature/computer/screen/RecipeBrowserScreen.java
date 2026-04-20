package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.ComputerRegistry;
import com.ultra.megamod.feature.dungeons.DungeonRegistry;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Recipe Browser computer app screen. Lets players browse all items in a searchable,
 * categorised grid and view MegaMod crafting recipes.
 */
public class RecipeBrowserScreen extends Screen {

    private final Screen parent;
    private EditBox searchBox;
    private int scroll = 0;
    private int maxScroll = 0;
    private String category = "All";
    private int selectedIndex = -1;
    private int recipePageIndex = 0;

    private List<ItemStack> allItems = new ArrayList<>();
    private List<ItemStack> filteredItems = new ArrayList<>();
    private List<ItemStack> megamodItems = new ArrayList<>();

    // Layout constants
    private static final int GRID_CELL = 20;
    private static final int GRID_PAD = 2;
    private static final int CELL_TOTAL = GRID_CELL + GRID_PAD;
    private static final String[] CATEGORIES = {"All", "Tools", "Armor", "Food", "Building", "Redstone", "MegaMod"};
    private static final int TAB_H = 16;
    private static final int TAB_GAP = 2;

    // Computed layout
    private int titleBarH;
    private int tabsY;
    private int gridLeft;
    private int gridTop;
    private int gridRight;
    private int gridBottom;
    private int gridCols;
    private int gridRows;
    private int detailLeft;
    private int detailTop;
    private int detailW;
    private int detailH;
    private int backX, backY, backW, backH;
    private int[] tabX;
    private int[] tabW;

    // --- MegaMod recipe definitions ---
    // Each recipe: Item -> list of RecipeEntry (supports multiple recipes per item)
    private static final Map<Item, List<RecipeEntry>> MEGAMOD_RECIPES = new LinkedHashMap<>();

    private record RecipeEntry(String type, int resultCount, ItemStack[] grid) {}

    static {
        // Helper: shaped 3x3
        addShaped(ComputerRegistry.COMPUTER_ITEM.get(), 1,
                Items.IRON_INGOT, Items.GLASS_PANE, Items.IRON_INGOT,
                Items.REDSTONE, Items.QUARTZ, Items.REDSTONE,
                Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT);

        addShaped(MuseumRegistry.MOB_NET_ITEM.get(), 2,
                Items.STRING, Items.STRING, Items.STRING,
                Items.STRING, Items.SLIME_BALL, Items.STRING,
                Items.STICK, Items.STICK, Items.STICK);

        addShaped(MuseumRegistry.MUSEUM_DOOR_ITEM.get(), 1,
                Items.GOLD_INGOT, Items.GOLD_INGOT, null,
                Items.OAK_PLANKS, Items.OAK_PLANKS, null,
                Items.OAK_PLANKS, Items.OAK_PLANKS, null);

        addShaped(DungeonRegistry.SOUL_ANCHOR.get(), 1,
                Items.CRYING_OBSIDIAN, Items.GOLD_INGOT, Items.CRYING_OBSIDIAN,
                Items.GOLD_INGOT, Items.NETHER_STAR, Items.GOLD_INGOT,
                Items.CRYING_OBSIDIAN, Items.GOLD_INGOT, Items.CRYING_OBSIDIAN);

        // Shapeless arrows
        addShapeless(DungeonEntityRegistry.CERULEAN_ARROW_ITEM.get(), 4,
                DungeonEntityRegistry.RAT_FANG.get(), Items.ARROW);

        addShapeless(DungeonEntityRegistry.CRYSTAL_ARROW_ITEM.get(), 4,
                DungeonEntityRegistry.SKELETON_BONE.get(), Items.ARROW);
    }

    private static void addShaped(Item result, int count,
                                   Item s0, Item s1, Item s2,
                                   Item s3, Item s4, Item s5,
                                   Item s6, Item s7, Item s8) {
        ItemStack[] grid = new ItemStack[9];
        Item[] slots = {s0, s1, s2, s3, s4, s5, s6, s7, s8};
        for (int i = 0; i < 9; i++) {
            grid[i] = slots[i] != null ? new ItemStack(slots[i]) : ItemStack.EMPTY;
        }
        MEGAMOD_RECIPES.computeIfAbsent(result, k -> new ArrayList<>())
                .add(new RecipeEntry("Crafting (Shaped)", count, grid));
    }

    private static void addShapeless(Item result, int count, Item... ingredients) {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = i < ingredients.length ? new ItemStack(ingredients[i]) : ItemStack.EMPTY;
        }
        MEGAMOD_RECIPES.computeIfAbsent(result, k -> new ArrayList<>())
                .add(new RecipeEntry("Crafting (Shapeless)", count, grid));
    }

    public RecipeBrowserScreen(Screen parent) {
        super(Component.literal("Recipe Browser"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.tabsY = this.titleBarH + 4;

        // Back button
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;

        // Calculate tab widths
        this.tabX = new int[CATEGORIES.length];
        this.tabW = new int[CATEGORIES.length];
        int tabStartX = 8;
        for (int i = 0; i < CATEGORIES.length; i++) {
            this.tabW[i] = this.font.width(CATEGORIES[i]) + 12;
            this.tabX[i] = tabStartX;
            tabStartX += this.tabW[i] + TAB_GAP;
        }

        // Detail panel on the right: 140px wide
        this.detailW = 140;
        this.detailLeft = this.width - this.detailW - 8;
        this.detailTop = this.tabsY + TAB_H + 6;
        this.detailH = this.height - this.detailTop - 8;

        // Grid area on the left
        this.gridLeft = 8;
        this.gridTop = this.detailTop + 22; // room for search box
        this.gridRight = this.detailLeft - 14; // gap + scrollbar
        this.gridBottom = this.height - 8;
        this.gridCols = Math.max(1, (this.gridRight - this.gridLeft) / CELL_TOTAL);
        this.gridRows = Math.max(1, (this.gridBottom - this.gridTop) / CELL_TOTAL);

        // Search box
        int searchW = Math.min(this.gridRight - this.gridLeft, 200);
        this.searchBox = new EditBox(this.font, this.gridLeft, this.detailTop, searchW, 16,
                Component.literal("Search..."));
        this.searchBox.setMaxLength(64);
        this.searchBox.setTextColor(-1);
        this.searchBox.setResponder(text -> {
            this.scroll = 0;
            this.selectedIndex = -1;
            this.recipePageIndex = 0;
            this.rebuildFilteredList();
        });
        this.addRenderableWidget(this.searchBox);

        // Build item lists
        buildAllItems();
        rebuildFilteredList();
    }

    private void buildAllItems() {
        this.allItems.clear();
        this.megamodItems.clear();

        Minecraft mc = Minecraft.getInstance();
        boolean isAdmin = mc.player != null && isAdminPlayer(mc.player.getGameProfile().name());

        // Build set of items with unlocked recipes (for non-admin players)
        Set<Item> unlockedItems = new HashSet<>();
        if (!isAdmin && mc.player != null) {
            net.minecraft.client.ClientRecipeBook recipeBook = mc.player.getRecipeBook();
            for (var collection : recipeBook.getCollections()) {
                for (var entry : collection.getRecipes()) {
                    var display = entry.display();
                    var result = display.result();
                    if (result instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay itemDisplay) {
                        unlockedItems.add(itemDisplay.stack().getItem());
                    } else if (result instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay itemSlotDisplay) {
                        unlockedItems.add(itemSlotDisplay.item().value());
                    }
                }
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) continue;
            String id = BuiltInRegistries.ITEM.getKey(item).toString();

            // Non-admin: only show items with unlocked recipes, basic items, or MegaMod items with hardcoded recipes
            if (!isAdmin) {
                boolean isUnlocked = unlockedItems.contains(item);
                boolean isMegamodWithRecipe = id.startsWith("megamod:") && MEGAMOD_RECIPES.containsKey(item);
                boolean isBasicItem = isAlwaysVisible(item, id);
                if (!isUnlocked && !isMegamodWithRecipe && !isBasicItem) continue;
            }

            this.allItems.add(stack);
            if (id.startsWith("megamod:")) {
                this.megamodItems.add(stack);
            }
        }
    }

    /** Admin check on client side — matches AdminSystem.ADMIN_USERNAMES */
    private static boolean isAdminPlayer(String name) {
        return "NeverNotch".equals(name) || "Dev".equals(name);
    }

    /** Items that are always visible regardless of recipe unlock (basic materials, etc.) */
    private static boolean isAlwaysVisible(Item item, String id) {
        // Creative-only items, spawn eggs, and command blocks are hidden for non-admins
        if (id.contains("spawn_egg") || id.contains("command_block") || id.contains("barrier")
            || id.contains("structure_block") || id.contains("jigsaw") || id.contains("debug_stick")) {
            return false;
        }
        // Basic resources, natural items, and mob drops are always visible
        return true;
    }

    private void rebuildFilteredList() {
        this.filteredItems.clear();
        String searchText = this.searchBox != null ? this.searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";

        List<ItemStack> sourceList = "MegaMod".equals(this.category) ? this.megamodItems : this.allItems;

        for (ItemStack stack : sourceList) {
            if (!matchesCategory(stack)) continue;
            if (!searchText.isEmpty()) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
                if (!name.contains(searchText) && !id.contains(searchText)) continue;
            }
            this.filteredItems.add(stack);
        }

        // Recalculate max scroll
        int totalRows = (this.filteredItems.size() + this.gridCols - 1) / this.gridCols;
        int visibleRows = this.gridRows;
        this.maxScroll = Math.max(0, totalRows - visibleRows);
        this.scroll = Math.min(this.scroll, this.maxScroll);
    }

    private boolean matchesCategory(ItemStack stack) {
        if ("All".equals(this.category) || "MegaMod".equals(this.category)) return true;
        Item item = stack.getItem();
        String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
        return switch (this.category) {
            case "Tools" -> isTool(id, item);
            case "Armor" -> isArmor(id, item);
            case "Food" -> isFood(id, stack);
            case "Building" -> isBuilding(id, item);
            case "Redstone" -> isRedstone(id);
            default -> true;
        };
    }

    private boolean isTool(String id, Item item) {
        return id.contains("sword") || id.contains("pickaxe") || id.contains("axe")
                || id.contains("shovel") || id.contains("hoe") || id.contains("bow")
                || id.contains("crossbow") || id.contains("trident") || id.contains("shears")
                || id.contains("flint_and_steel") || id.contains("fishing_rod")
                || id.contains("_tome") || id.contains("_blade") || id.contains("_hammer")
                || id.contains("_fang") || id.contains("_glaive") || id.contains("fang_on");
    }

    private boolean isArmor(String id, Item item) {
        return id.contains("helmet") || id.contains("chestplate") || id.contains("leggings")
                || id.contains("boots") || id.contains("shield") || id.contains("elytra")
                || id.contains("_crown") || id.contains("_belt") || id.contains("_robe")
                || id.contains("_necklace") || id.contains("_locket") || id.contains("_ring")
                || id.contains("_glove") || id.contains("_mitten") || id.contains("_walker")
                || id.contains("_skates") || id.contains("_breaker") || id.contains("_boot")
                || id.contains("_hand") || id.contains("quiver");
    }

    private boolean isBuilding(String id, Item item) {
        return id.contains("bricks") || id.contains("planks") || id.contains("stone")
                || id.contains("log") || id.contains("slab") || id.contains("stairs")
                || id.contains("wall") || id.contains("fence") || id.contains("door")
                || id.contains("glass") || id.contains("concrete") || id.contains("terracotta")
                || id.contains("wool");
    }

    private boolean isRedstone(String id) {
        return id.contains("redstone") || id.contains("piston") || id.contains("repeater")
                || id.contains("comparator") || id.contains("observer") || id.contains("hopper")
                || id.contains("dropper") || id.contains("dispenser") || id.contains("lever")
                || id.contains("button") || id.contains("pressure_plate") || id.contains("lamp")
                || id.contains("tripwire") || id.contains("target");
    }

    private boolean isFood(String id, ItemStack stack) {
        // Check component-based food flag first
        try {
            if (stack.getItem().components().has(net.minecraft.core.component.DataComponents.FOOD)) {
                return true;
            }
        } catch (Exception ignored) {}
        // Fallback: common food item names
        return id.contains("apple") || id.contains("beef") || id.contains("pork")
                || id.contains("chicken") || id.contains("mutton") || id.contains("rabbit")
                || id.contains("cod") || id.contains("salmon") || id.contains("bread")
                || id.contains("cookie") || id.contains("melon_slice") || id.contains("carrot")
                || id.contains("potato") || id.contains("beetroot") || id.contains("stew")
                || id.contains("cake") || id.contains("pie") || id.contains("spider_eye")
                || id.contains("rotten_flesh") || id.contains("chorus_fruit")
                || id.contains("dried_kelp") || id.contains("sweet_berries")
                || id.contains("glow_berries") || id.contains("honey_bottle")
                || id.contains("golden_apple") || id.contains("enchanted_golden_apple")
                || id.contains("suspicious_stew") || id.contains("strange_meat");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Full background
        g.fill(0, 0, this.width, this.height, -16120316);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Recipe Browser", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW
                && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Item count label in title bar
        String countStr = this.filteredItems.size() + " items";
        int countW = this.font.width(countStr);
        g.drawString(this.font, countStr, this.width - countW - 10, titleY, UIHelper.GOLD_DARK, false);

        // Category tabs
        renderTabs(g, mouseX, mouseY);

        // Item grid background
        UIHelper.drawInsetPanel(g, this.gridLeft - 2, this.gridTop - 2,
                this.gridRight - this.gridLeft + 4, this.gridBottom - this.gridTop + 4);

        // Item grid
        renderItemGrid(g, mouseX, mouseY);

        // Scrollbar
        if (this.maxScroll > 0) {
            float scrollProgress = (float) this.scroll / (float) this.maxScroll;
            UIHelper.drawScrollbar(g, this.gridRight + 2, this.gridTop, this.gridBottom - this.gridTop, scrollProgress);
        }

        // Detail panel
        renderDetailPanel(g, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            boolean selected = CATEGORIES[i].equals(this.category);
            boolean hovered = mouseX >= this.tabX[i] && mouseX < this.tabX[i] + this.tabW[i]
                    && mouseY >= this.tabsY && mouseY < this.tabsY + TAB_H;
            UIHelper.drawTab(g, this.tabX[i], this.tabsY, this.tabW[i], TAB_H, selected);
            int textColor = selected ? UIHelper.GOLD_BRIGHT : (hovered ? UIHelper.CREAM_TEXT : UIHelper.GOLD_DARK);
            int textX = this.tabX[i] + (this.tabW[i] - this.font.width(CATEGORIES[i])) / 2;
            int textY = this.tabsY + (TAB_H - 9) / 2;
            g.drawString(this.font, CATEGORIES[i], textX, textY, textColor, false);
        }
    }

    private void renderItemGrid(GuiGraphics g, int mouseX, int mouseY) {
        g.enableScissor(this.gridLeft, this.gridTop, this.gridRight, this.gridBottom);

        int startIdx = this.scroll * this.gridCols;
        int endIdx = Math.min(this.filteredItems.size(), startIdx + this.gridCols * this.gridRows);

        for (int idx = startIdx; idx < endIdx; idx++) {
            int localIdx = idx - startIdx;
            int col = localIdx % this.gridCols;
            int row = localIdx / this.gridCols;
            int x = this.gridLeft + col * CELL_TOTAL;
            int y = this.gridTop + row * CELL_TOTAL;

            boolean isSelected = idx == this.selectedIndex;
            boolean isHovered = mouseX >= x && mouseX < x + GRID_CELL
                    && mouseY >= y && mouseY < y + GRID_CELL;

            UIHelper.drawSlot(g, x, y, GRID_CELL, isHovered, isSelected);

            ItemStack stack = this.filteredItems.get(idx);
            g.renderItem(stack, x + 2, y + 2);
        }

        g.disableScissor();
    }

    private void renderDetailPanel(GuiGraphics g, int mouseX, int mouseY) {
        UIHelper.drawCard(g, this.detailLeft, this.detailTop, this.detailW, this.detailH);

        if (this.selectedIndex < 0 || this.selectedIndex >= this.filteredItems.size()) {
            // No item selected
            String hint1 = "Select an";
            String hint2 = "item to view";
            String hint3 = "its details";
            int centerX = this.detailLeft + this.detailW / 2;
            int centerY = this.detailTop + this.detailH / 2 - 14;
            UIHelper.drawCenteredLabel(g, this.font, hint1, centerX, centerY);
            UIHelper.drawCenteredLabel(g, this.font, hint2, centerX, centerY + 12);
            UIHelper.drawCenteredLabel(g, this.font, hint3, centerX, centerY + 24);
            return;
        }

        ItemStack selectedStack = this.filteredItems.get(this.selectedIndex);
        Item selectedItem = selectedStack.getItem();
        String itemName = selectedStack.getHoverName().getString();
        String itemId = BuiltInRegistries.ITEM.getKey(selectedItem).toString();

        int px = this.detailLeft + 6;
        int py = this.detailTop + 8;

        // Render item icon large-ish (centered)
        int iconX = this.detailLeft + (this.detailW - 16) / 2;
        g.renderItem(selectedStack, iconX, py);
        py += 20;

        // Item name (word-wrap if needed)
        drawWrappedText(g, itemName, px, py, this.detailW - 12, UIHelper.GOLD_BRIGHT);
        int nameLines = Math.max(1, (this.font.width(itemName) + this.detailW - 14) / (this.detailW - 12));
        py += nameLines * 10 + 2;

        // Item ID
        drawWrappedText(g, itemId, px, py, this.detailW - 12, UIHelper.GOLD_DARK);
        int idLines = Math.max(1, (this.font.width(itemId) + this.detailW - 14) / (this.detailW - 12));
        py += idLines * 10 + 2;

        // Stack size
        g.drawString(this.font, "Max Stack: " + selectedStack.getMaxStackSize(), px, py, UIHelper.CREAM_TEXT, false);
        py += 12;

        // Divider
        if (py + 4 < this.detailTop + this.detailH - 10) {
            UIHelper.drawHorizontalDivider(g, px, py, this.detailW - 12);
            py += 6;
        }

        // Check for MegaMod recipe
        boolean isMegaMod = itemId.startsWith("megamod:");
        List<RecipeEntry> recipes = MEGAMOD_RECIPES.get(selectedItem);

        if (recipes != null && !recipes.isEmpty()) {
            // Has a known MegaMod recipe
            int totalRecipes = recipes.size();
            this.recipePageIndex = Math.max(0, Math.min(this.recipePageIndex, totalRecipes - 1));
            RecipeEntry recipe = recipes.get(this.recipePageIndex);

            // Recipe type label
            g.drawString(this.font, recipe.type, px, py, UIHelper.BLUE_ACCENT, false);
            py += 11;

            // Multi-recipe navigation
            if (totalRecipes > 1) {
                String pageStr = (this.recipePageIndex + 1) + "/" + totalRecipes;
                int prevX = px;
                int nextX = px + 24 + this.font.width(pageStr) + 8;
                boolean prevHover = mouseX >= prevX && mouseX < prevX + 12
                        && mouseY >= py && mouseY < py + 10;
                boolean nextHover = mouseX >= nextX && mouseX < nextX + 12
                        && mouseY >= py && mouseY < py + 10;
                UIHelper.drawButton(g, prevX, py, 12, 10, prevHover);
                g.drawString(this.font, "<", prevX + 3, py + 1, UIHelper.CREAM_TEXT, false);
                g.drawString(this.font, pageStr, prevX + 16, py + 1, UIHelper.CREAM_TEXT, false);
                UIHelper.drawButton(g, nextX, py, 12, 10, nextHover);
                g.drawString(this.font, ">", nextX + 3, py + 1, UIHelper.CREAM_TEXT, false);
                py += 14;
            }

            // 3x3 crafting grid
            int gridSize = 18;
            int gridGap = 1;
            int totalGridW = 3 * gridSize + 2 * gridGap;
            int craftGridX = this.detailLeft + (this.detailW - totalGridW - 28) / 2;
            int craftGridY = py;

            if (craftGridY + 3 * (gridSize + gridGap) < this.detailTop + this.detailH - 4) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int slotX = craftGridX + col * (gridSize + gridGap);
                        int slotY = craftGridY + row * (gridSize + gridGap);
                        int idx = row * 3 + col;
                        ItemStack ingredient = recipe.grid[idx];
                        boolean slotHover = mouseX >= slotX && mouseX < slotX + gridSize
                                && mouseY >= slotY && mouseY < slotY + gridSize;
                        UIHelper.drawSlot(g, slotX, slotY, gridSize, slotHover, false);
                        if (!ingredient.isEmpty()) {
                            g.renderItem(ingredient, slotX + 1, slotY + 1);
                        }
                    }
                }

                // Arrow
                int arrowX = craftGridX + totalGridW + 4;
                int arrowY = craftGridY + (3 * (gridSize + gridGap)) / 2 - 5;
                g.drawString(this.font, "=>", arrowX, arrowY, UIHelper.GOLD_MID, false);

                // Result
                int resultX = arrowX + 18;
                int resultY = craftGridY + (3 * (gridSize + gridGap)) / 2 - gridSize / 2;
                UIHelper.drawSlot(g, resultX, resultY, gridSize, false, true);
                g.renderItem(selectedStack, resultX + 1, resultY + 1);
                if (recipe.resultCount > 1) {
                    String countStr = "x" + recipe.resultCount;
                    g.drawString(this.font, countStr, resultX, resultY + gridSize + 2, UIHelper.CREAM_TEXT, false);
                }

                py = craftGridY + 3 * (gridSize + gridGap) + 6;
            }

            // Ingredient tooltip on hover
            if (craftGridY + 3 * (gridSize + gridGap) < this.detailTop + this.detailH) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int slotX = craftGridX + col * (gridSize + gridGap);
                        int slotY = craftGridY + row * (gridSize + gridGap);
                        int idx = row * 3 + col;
                        ItemStack ingredient = recipe.grid[idx];
                        if (!ingredient.isEmpty() && mouseX >= slotX && mouseX < slotX + gridSize
                                && mouseY >= slotY && mouseY < slotY + gridSize) {
                            String ingName = ingredient.getHoverName().getString();
                            int ttW = this.font.width(ingName) + 8;
                            int ttX = Math.min(mouseX + 8, this.width - ttW - 4);
                            int ttY = mouseY - 14;
                            UIHelper.drawTooltipBackground(g, ttX, ttY, ttW, 14);
                            g.drawString(this.font, ingName, ttX + 4, ttY + 3, UIHelper.CREAM_TEXT, false);
                        }
                    }
                }
            }
        } else if (isMegaMod) {
            // MegaMod item but no recipe data -- dungeon drop or special
            g.drawString(this.font, "No crafting", px, py, UIHelper.GOLD_DARK, false);
            py += 11;
            g.drawString(this.font, "recipe.", px, py, UIHelper.GOLD_DARK, false);
            py += 14;
            g.drawString(this.font, "Obtained via:", px, py, UIHelper.CREAM_TEXT, false);
            py += 11;
            String source = getObtainSource(itemId);
            drawWrappedText(g, source, px, py, this.detailW - 12, UIHelper.BLUE_ACCENT);
        } else {
            // Vanilla item
            g.drawString(this.font, "Vanilla item", px, py, UIHelper.GOLD_DARK, false);
            py += 14;
            g.drawString(this.font, "Use JEI or", px, py, UIHelper.CREAM_TEXT, false);
            py += 11;
            g.drawString(this.font, "/recipe for", px, py, UIHelper.CREAM_TEXT, false);
            py += 11;
            g.drawString(this.font, "full recipes.", px, py, UIHelper.CREAM_TEXT, false);
        }

        // Item tooltip on grid hover
        renderGridTooltip(g, mouseX, mouseY);
    }

    private void renderGridTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (mouseX < this.gridLeft || mouseX >= this.gridRight
                || mouseY < this.gridTop || mouseY >= this.gridBottom) return;

        int col = (mouseX - this.gridLeft) / CELL_TOTAL;
        int row = (mouseY - this.gridTop) / CELL_TOTAL;
        if (col >= this.gridCols || row >= this.gridRows) return;

        int idx = (this.scroll + row) * this.gridCols + col;
        if (idx < 0 || idx >= this.filteredItems.size()) return;

        ItemStack stack = this.filteredItems.get(idx);
        String name = stack.getHoverName().getString();
        int ttW = this.font.width(name) + 8;
        int ttX = Math.min(mouseX + 12, this.width - ttW - 4);
        int ttY = mouseY - 14;
        UIHelper.drawTooltipBackground(g, ttX, ttY, ttW, 14);
        g.drawString(this.font, name, ttX + 4, ttY + 3, UIHelper.CREAM_TEXT, false);
    }

    private void drawWrappedText(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        if (maxWidth <= 0) return;
        // Simple word wrap
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (this.font.width(test) > maxWidth && !line.isEmpty()) {
                g.drawString(this.font, line.toString(), x, lineY, color, false);
                lineY += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) {
            g.drawString(this.font, line.toString(), x, lineY, color, false);
        }
    }

    private String getObtainSource(String itemId) {
        if (itemId.contains("dungeon_key")) return "Accept a quest from the Royal Herald";
        if (itemId.contains("void_shard")) return "Dungeon Slime drops";
        if (itemId.contains("boss_trophy")) return "Dungeon boss defeat reward";
        if (itemId.contains("dungeon_map")) return "Found in dungeon treasure chests";
        if (itemId.contains("infernal_essence")) return "Infernal tier boss drops";
        if (itemId.contains("warp_stone")) return "Found in dungeon chests";
        if (itemId.contains("trophy")) return "Defeating the corresponding boss";
        if (itemId.contains("rat_fang")) return "Dropped by Dungeon Rats";
        if (itemId.contains("fang_on_a_stick")) return "Found in boss treasure chests";
        if (itemId.contains("skeleton_bone")) return "Dropped by Undead Knights";
        if (itemId.contains("skeleton_head")) return "Rare Undead Knight drop";
        if (itemId.contains("ossukage_sword")) return "Dropped by Ossukage boss";
        if (itemId.contains("dungeon_mini_key")) return "Found in dungeon chests";
        if (itemId.contains("strange_meat")) return "Dungeon mob drops";
        if (itemId.contains("living_divining_rod")) return "Dungeon treasure chests";
        if (itemId.contains("absorption_orb")) return "Dungeon treasure chests";
        if (itemId.contains("cerulean_ingot")) return "Dungeon mob drops";
        if (itemId.contains("crystalline_shard")) return "Dungeon boss drops";
        if (itemId.contains("spectral_silk")) return "Hollow entity drops";
        if (itemId.contains("umbra_ingot")) return "Higher-tier dungeon drops";
        if (itemId.contains("fog_wall") || itemId.contains("dungeon_altar")
                || itemId.contains("dungeon_gate") || itemId.contains("dungeon_keyhole"))
            return "Dungeon structure block (creative only)";
        if (itemId.contains("quiver") || itemId.contains("booster") || itemId.contains("robe")
                || itemId.contains("belt") || itemId.contains("necklace") || itemId.contains("locket")
                || itemId.contains("walker") || itemId.contains("skates") || itemId.contains("breaker")
                || itemId.contains("glove") || itemId.contains("mitten") || itemId.contains("hand")
                || itemId.contains("ring") || itemId.contains("inhibitor") || itemId.contains("glaive")
                || itemId.contains("ham") || itemId.contains("dissector") || itemId.contains("mirror")
                || itemId.contains("flute") || itemId.contains("sack") || itemId.contains("flask")
                || itemId.contains("crown") || itemId.contains("boot"))
            return "Relic - found via exploration or Researching Table";
        // Legendary "tome" weapons (vampiric_tome, ebonchill, etc.) were removed; the
        // previous branch here returned a description for items that no longer exist.
        if (itemId.contains("museum") || itemId.contains("pedestal") || itemId.contains("captured_mob"))
            return "Museum system item";
        if (itemId.contains("phone")) return "Computer system item";
        if (itemId.contains("computer") || itemId.contains("keyboard") || itemId.contains("mouse")
                || itemId.contains("monitor") || itemId.contains("flatscreen")) return "Computer peripheral - craftable";
        if (itemId.contains("spawn_egg")) return "Creative mode only";
        return "Special MegaMod item";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        if (mx >= this.backX && mx < this.backX + this.backW
                && my >= this.backY && my < this.backY + this.backH) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        // Category tabs
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (mx >= this.tabX[i] && mx < this.tabX[i] + this.tabW[i]
                    && my >= this.tabsY && my < this.tabsY + TAB_H) {
                this.category = CATEGORIES[i];
                this.scroll = 0;
                this.selectedIndex = -1;
                this.recipePageIndex = 0;
                rebuildFilteredList();
                return true;
            }
        }

        // Grid click
        if (mx >= this.gridLeft && mx < this.gridRight
                && my >= this.gridTop && my < this.gridBottom) {
            int col = (mx - this.gridLeft) / CELL_TOTAL;
            int row = (my - this.gridTop) / CELL_TOTAL;
            if (col < this.gridCols && row < this.gridRows) {
                int idx = (this.scroll + row) * this.gridCols + col;
                if (idx >= 0 && idx < this.filteredItems.size()) {
                    this.selectedIndex = idx;
                    this.recipePageIndex = 0;
                    return true;
                }
            }
        }

        // Recipe page navigation arrows
        if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredItems.size()) {
            Item selectedItem = this.filteredItems.get(this.selectedIndex).getItem();
            List<RecipeEntry> recipes = MEGAMOD_RECIPES.get(selectedItem);
            if (recipes != null && recipes.size() > 1) {
                // Recalculate the positions used during render
                int px = this.detailLeft + 6;
                int py = this.detailTop + 8;
                py += 20; // icon
                String itemName = this.filteredItems.get(this.selectedIndex).getHoverName().getString();
                int nameLines = Math.max(1, (this.font.width(itemName) + this.detailW - 14) / (this.detailW - 12));
                py += nameLines * 10 + 2;
                String itemId = BuiltInRegistries.ITEM.getKey(selectedItem).toString();
                int idLines = Math.max(1, (this.font.width(itemId) + this.detailW - 14) / (this.detailW - 12));
                py += idLines * 10 + 2;
                py += 12; // stack size
                py += 6; // divider
                py += 11; // recipe type label

                int prevX = px;
                String pageStr = (this.recipePageIndex + 1) + "/" + recipes.size();
                int nextX = px + 24 + this.font.width(pageStr) + 8;

                if (mx >= prevX && mx < prevX + 12 && my >= py && my < py + 10) {
                    this.recipePageIndex = (this.recipePageIndex - 1 + recipes.size()) % recipes.size();
                    return true;
                }
                if (mx >= nextX && mx < nextX + 12 && my >= py && my < py + 10) {
                    this.recipePageIndex = (this.recipePageIndex + 1) % recipes.size();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= this.gridLeft && mouseX < this.gridRight
                && mouseY >= this.gridTop && mouseY < this.gridBottom) {
            this.scroll -= (int) scrollY;
            this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll));
            return true;
        }
        return false;
    }

    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();
        if (this.searchBox != null && this.searchBox.isFocused()) {
            return super.keyPressed(event);
        }
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
