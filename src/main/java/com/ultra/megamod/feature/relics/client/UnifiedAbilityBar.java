package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.RelicSpellAssignments;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side singleton tracking accessory ability bar UI state:
 * - Which accessory is selected (G+Scroll cycles)
 * - Which ability is selected per accessory (G tap flips)
 *
 * <p>Phase H: weapon half removed — all weapon casting (tomes, Arsenal uniques,
 * wands, staves) is driven by SpellEngine's own HUD/hotbar and cast controller.
 */
public class UnifiedAbilityBar {

    // --- Accessory state ---
    private static List<AccessoryAbilityEntry> accessoryEntries = new ArrayList<>();
    private static int selectedAccessoryIndex = 0;
    private static final Map<String, Integer> selectedAbilityPerSlot = new HashMap<>();

    // --- Held-item change detection (offhand only now; used to rebuild accessories) ---
    private static ItemStack lastOffhandItem = ItemStack.EMPTY;

    // --- Records ---
    public record AccessoryAbilityEntry(String slotName, String itemId, String relicName,
                                        List<String> castableAbilities) {}

    // --- Getters ---
    public static List<AccessoryAbilityEntry> getAccessoryEntries() { return accessoryEntries; }
    public static int getSelectedAccessoryIndex() { return selectedAccessoryIndex; }

    public static int getSelectedAbilityIndex(String slotName) {
        return selectedAbilityPerSlot.getOrDefault(slotName, 0);
    }

    public static AccessoryAbilityEntry getSelectedAccessory() {
        if (accessoryEntries.isEmpty()) return null;
        if (selectedAccessoryIndex >= accessoryEntries.size()) selectedAccessoryIndex = 0;
        return accessoryEntries.get(selectedAccessoryIndex);
    }

    public static boolean hasAccessoryAbilities() { return !accessoryEntries.isEmpty(); }
    public static int getAccessoryCount() { return accessoryEntries.size(); }

    // --- Rebuild from sync data ---

    public static void rebuildAccessories() {
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        accessoryEntries = new ArrayList<>();

        if (equipped != null && !equipped.isEmpty()) {
            for (Map.Entry<String, String> entry : equipped.entrySet()) {
                String slotName = entry.getKey();
                String itemId = entry.getValue();
                if ("NONE".equals(slotName)) continue;

                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                if (item instanceof RelicItem relicItem) {
                    // Accessory castable abilities are the relic's SpellContainer spell ids
                    // (see RelicSpellAssignments). Each entry id is a spell, and the UI resolves
                    // its display name from the ability metadata.
                    List<String> spellIds = RelicSpellAssignments.forItem(itemId);
                    List<String> castable = new ArrayList<>();
                    for (String sid : spellIds) {
                        RelicSpellAssignments.SpellMeta meta = RelicSpellAssignments.metaFor(sid);
                        // Skip purely-passive spells from the R-tap cycle.
                        if (meta == null) continue;
                        // SpellEngine itself decides whether a PASSIVE spell is castable
                        // on attempt (it'll simply no-op).
                        castable.add(sid);
                    }
                    if (!castable.isEmpty()) {
                        accessoryEntries.add(new AccessoryAbilityEntry(
                                slotName, itemId, relicItem.getRelicName(), castable));
                    }
                }
            }
        }

        if (selectedAccessoryIndex >= accessoryEntries.size()) {
            selectedAccessoryIndex = 0;
        }
    }

    // --- Scroll handlers ---

    public static void onScrollWithG(int direction) {
        if (accessoryEntries.isEmpty()) return;
        selectedAccessoryIndex = (selectedAccessoryIndex + direction + accessoryEntries.size())
                % accessoryEntries.size();
    }

    public static void onTapG() {
        if (accessoryEntries.isEmpty()) return;
        if (selectedAccessoryIndex >= accessoryEntries.size()) selectedAccessoryIndex = 0;
        AccessoryAbilityEntry entry = accessoryEntries.get(selectedAccessoryIndex);
        int currentIdx = selectedAbilityPerSlot.getOrDefault(entry.slotName(), 0);
        int nextIdx = (currentIdx + 1) % entry.castableAbilities().size();
        selectedAbilityPerSlot.put(entry.slotName(), nextIdx);
    }

    // --- Per-tick update ---

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Track offhand changes so equipment bar refreshes its display
        ItemStack currentOffhand = mc.player.getOffhandItem();
        if (!ItemStack.isSameItem(currentOffhand, lastOffhandItem)) {
            lastOffhandItem = currentOffhand.copy();
            rebuildAccessories();
        }
    }

    /** Kept for HUD compatibility; offhand no longer contributes castable entries. */
    public static boolean hasShieldAbilities() {
        return false;
    }
}
