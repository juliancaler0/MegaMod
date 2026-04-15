package com.ultra.megamod.feature.hud;

import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Adds a search box above any container screen (inventory, chests, etc).
 * Matching items get a gold highlight, non-matching get dimmed.
 * Skips creative inventory (has its own search tab).
 * Leaves space on the right for the sort button (ContainerScreenSortMixin).
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class InventorySearchOverlay {

    private static EditBox searchBox = null;
    private static String searchText = "";

    /**
     * Skip the overlay on screens that manage their own widgets above the
     * panel (title bar, tabs, etc.). The overlay positions its EditBox at
     * {@code guiTop - 18} which collides with any screen that offsets its
     * title above the standard inventory background.
     */
    private static boolean shouldSkip(AbstractContainerScreen<?> screen) {
        if (screen instanceof CreativeModeInventoryScreen) return true;
        // Spell Binding table offsets its title by -16 and crashes its title
        // into our search box. Accessories uses a bespoke multi-panel layout
        // with its own group-filter widgets. Both opt out.
        String cls = screen.getClass().getName();
        if (cls.equals("com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingScreen")) return true;
        if (cls.equals("com.ultra.megamod.lib.accessories.client.gui.AccessoriesScreen")) return true;
        return false;
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> container)) return;
        if (shouldSkip(container)) return;

        Minecraft mc = Minecraft.getInstance();
        // Leave 22px on right for the sort button (16w + 2 padding + 4 gap)
        int sortBtnSpace = 22;
        int boxW = container.getXSize() - sortBtnSpace;
        int boxX = container.getGuiLeft();
        int boxY = container.getGuiTop() - 18;

        searchBox = new EditBox(mc.font, boxX, boxY, boxW, 14, Component.literal("Search..."));
        searchBox.setMaxLength(30);
        searchBox.setBordered(true);
        searchBox.setVisible(true);
        searchBox.setHint(Component.literal("Search items..."));
        searchBox.setResponder(text -> searchText = text.toLowerCase());
        searchText = "";

        event.addListener(searchBox);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> container)) return;
        if (shouldSkip(container)) return;
        if (searchBox == null || searchText.isEmpty()) return;

        GuiGraphics g = event.getGuiGraphics();
        int guiLeft = container.getGuiLeft();
        int guiTop = container.getGuiTop();

        for (Slot slot : container.getMenu().slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            int slotX = guiLeft + slot.x;
            int slotY = guiTop + slot.y;

            String name = stack.getHoverName().getString().toLowerCase();
            if (name.contains(searchText)) {
                // Gold highlight border for matches
                g.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0xFFFFAA00);
                g.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0xFFFFAA00);
                g.fill(slotX - 1, slotY, slotX, slotY + 16, 0xFFFFAA00);
                g.fill(slotX + 16, slotY, slotX + 17, slotY + 16, 0xFFFFAA00);
            } else {
                // Dim non-matching items
                g.fill(slotX, slotY, slotX + 16, slotY + 16, 0x88000000);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> container)) return;
        if (shouldSkip(container)) return;
        if (searchBox == null || !searchBox.isFocused()) return;

        // Don't let inventory-close key (E) close the screen while typing
        int key = event.getKeyCode();
        if (key != 256) { // 256 = Escape — always allow escape
            event.setCanceled(true);
            searchBox.keyPressed(new KeyEvent(key, event.getScanCode(), event.getModifiers()));
        }
    }

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> container)) return;
        if (shouldSkip(container)) return;
        if (searchBox == null || !searchBox.isFocused()) return;
        event.setCanceled(true);
        searchBox.charTyped(new CharacterEvent(event.getCodePoint(), event.getModifiers()));
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            searchBox = null;
            searchText = "";
        }
    }
}
