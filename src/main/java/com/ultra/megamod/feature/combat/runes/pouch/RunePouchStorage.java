package com.ultra.megamod.feature.combat.runes.pouch;

import com.ultra.megamod.feature.combat.runes.RuneRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Serialises rune-pouch contents to/from {@link ItemStack} CUSTOM_DATA using a
 * flat {Slot, id, count} format. Rune-only scope means we don't need full item
 * stack serialisation — just the rune id and count.
 */
public final class RunePouchStorage {
    private RunePouchStorage() {}

    public static final String KEY_ITEMS = "rune_pouch_items";

    /** Reads the stored rune stacks off the pouch into a fresh container. */
    public static SimpleContainer load(ItemStack pouchStack) {
        int size = capacityFor(pouchStack);
        SimpleContainer container = new SimpleContainer(size);
        CustomData data = pouchStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        if (!tag.contains(KEY_ITEMS)) return container;
        ListTag list = tag.getListOrEmpty(KEY_ITEMS);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            int slot = entry.getIntOr("Slot", 0);
            String id = entry.getStringOr("Id", "");
            int count = entry.getIntOr("Count", 0);
            if (slot < 0 || slot >= size || id.isEmpty() || count <= 0) continue;
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
            if (item == null || item == net.minecraft.world.item.Items.AIR) continue;
            container.setItem(slot, new ItemStack(item, count));
        }
        return container;
    }

    /** Writes the current container contents back into the pouch stack. */
    public static void save(ItemStack pouchStack, Container container) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (s.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.putString("Id", BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
            entry.putInt("Count", s.getCount());
            list.add(entry);
        }
        CustomData existing = pouchStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        tag.put(KEY_ITEMS, list);
        pouchStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Pouches only accept rune items. */
    public static boolean isValidRune(ItemStack stack) {
        if (stack.isEmpty()) return true;
        Item item = stack.getItem();
        return item == RuneRegistry.ARCANE_RUNE.get()
                || item == RuneRegistry.FIRE_RUNE.get()
                || item == RuneRegistry.FROST_RUNE.get()
                || item == RuneRegistry.HEALING_RUNE.get()
                || item == RuneRegistry.LIGHTNING_RUNE.get()
                || item == RuneRegistry.SOUL_RUNE.get();
    }

    /** Attempt to consume runes directly from a pouch (used by the spell system). */
    public static int consumeFromPouch(ItemStack pouchStack, Item runeItem, int requested) {
        CustomData existing = pouchStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        if (!tag.contains(KEY_ITEMS)) return 0;
        ListTag list = tag.getListOrEmpty(KEY_ITEMS);
        int remaining = requested;
        ListTag rebuilt = new ListTag();
        String targetId = BuiltInRegistries.ITEM.getKey(runeItem).toString();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            String id = entry.getStringOr("Id", "");
            int count = entry.getIntOr("Count", 0);
            int slot = entry.getIntOr("Slot", 0);
            if (id.equals(targetId) && remaining > 0) {
                int take = Math.min(remaining, count);
                count -= take;
                remaining -= take;
            }
            if (count > 0 && !id.isEmpty()) {
                CompoundTag out = new CompoundTag();
                out.putInt("Slot", slot);
                out.putString("Id", id);
                out.putInt("Count", count);
                rebuilt.add(out);
            }
        }
        tag.put(KEY_ITEMS, rebuilt);
        pouchStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return requested - remaining;
    }

    public static int capacityFor(ItemStack pouchStack) {
        return RunePouchType.of(pouchStack.getItem()).slots();
    }
}
