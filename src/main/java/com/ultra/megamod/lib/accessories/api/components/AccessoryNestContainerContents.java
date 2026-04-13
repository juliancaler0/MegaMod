package com.ultra.megamod.lib.accessories.api.components;

import com.ultra.megamod.lib.accessories.api.core.Accessory;
import com.ultra.megamod.lib.accessories.api.core.AccessoryNestUtils;
import com.ultra.megamod.lib.accessories.api.core.AccessoryRegistry;
import com.ultra.megamod.lib.accessories.api.events.SlotStateChange;
import com.ultra.megamod.lib.accessories.api.slot.SlotEntryReference;
import com.ultra.megamod.lib.accessories.api.slot.SlotPath;
import com.ultra.megamod.lib.accessories.api.slot.SlotPathWithStack;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.CodecUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class AccessoryNestContainerContents {

    public static final AccessoryNestContainerContents EMPTY = new AccessoryNestContainerContents(List.of());

    public static final Endec<AccessoryNestContainerContents> ENDEC = StructEndecBuilder.of(
            CodecUtils.toEndec(ItemStack.OPTIONAL_CODEC).listOf().fieldOf("accessories", AccessoryNestContainerContents::accessories),
            AccessoryNestContainerContents::new
    );

    private final List<ItemStack> accessories;

    private final Map<Integer, SlotStateChange> slotChanges = new Int2ObjectOpenHashMap<>();

    public AccessoryNestContainerContents(List<ItemStack> accessories) {
        this.accessories = accessories;
    }

    public AccessoryNestContainerContents setStack(int index, ItemStack stack) {
        var accessories = new ArrayList<>(accessories());

        accessories.set(index, stack);

        var contents = new AccessoryNestContainerContents(accessories);

        contents.slotChanges.putAll(slotChanges);
        contents.slotChanges.put(index, SlotStateChange.REPLACEMENT);

        return contents;
    }

    public AccessoryNestContainerContents addStack(ItemStack stack) {
        var accessories = new ArrayList<>(accessories());

        var index = accessories.size();

        accessories.add(stack);

        var contents = new AccessoryNestContainerContents(accessories);

        contents.slotChanges.putAll(slotChanges);
        contents.slotChanges.put(index, SlotStateChange.REPLACEMENT);

        return contents;
    }

    public Map<Integer, SlotStateChange> slotChanges() {
        return this.slotChanges;
    }

    public Map<ItemStack, Accessory> getMap() {
        var map = new LinkedHashMap<ItemStack, Accessory>();

        this.accessories().forEach(stack1 -> map.put(stack1, AccessoryRegistry.getAccessoryOrDefault(stack1)));

        return map;
    }

    public Map<SlotEntryReference, Accessory> getMap(SlotReference slotReference) {
        var map = new LinkedHashMap<SlotEntryReference, Accessory>();

        var innerStacks = this.accessories();

        for (int i = 0; i < innerStacks.size(); i++) {
            var innerStack = innerStacks.get(i);

            if (innerStack.isEmpty()) continue;

            map.put(new SlotEntryReference(SlotPath.cloneWithInnerIndex(slotReference, i), innerStack), AccessoryRegistry.getAccessoryOrDefault(innerStack));
        }

        return map;
    }

    public Map<SlotPathWithStack, Accessory> getMapWithPaths(SlotPath slotPath) {
        var map = new LinkedHashMap<SlotPathWithStack, Accessory>();

        var innerStacks = this.accessories();

        for (int i = 0; i < innerStacks.size(); i++) {
            var innerStack = innerStacks.get(i);

            if (innerStack.isEmpty()) continue;

            map.put(SlotPathWithStack.of(SlotPath.cloneWithInnerIndex(slotPath, i), innerStack), AccessoryRegistry.getAccessoryOrDefault(innerStack));
        }

        return map;
    }

    public List<ItemStack> accessories() {
        return accessories;
    }

    public void iterateStacks(BiConsumer<Integer, ItemStack> consumer) {
        for (int i = 0; i < this.accessories.size(); i++) {
            var innerStack = this.accessories.get(i);

            if (innerStack.isEmpty()) continue;

            consumer.accept(i, innerStack);
        }
    }

    public <T> @Nullable T iterateStacks(BiFunction<Integer, ItemStack, @Nullable T> function) {
        return iterateStacks(function, (AccessoryNestUtils.DefaultBehavior<T>) AccessoryNestUtils.DefaultBehavior.INSTANCE);
    }

    public <T> @Nullable T iterateStacks(BiFunction<Integer, ItemStack, @Nullable T> function, AccessoryNestUtils.DefaultBehavior<T> behavior) {
        for (int i = 0; i < this.accessories.size(); i++) {
            var innerStack = this.accessories.get(i);

            if (innerStack.isEmpty()) continue;

            var value = function.apply(i, innerStack);

            if(!behavior.isDefaulted(value)) break;
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AccessoryNestContainerContents) obj;
        return Objects.equals(this.accessories, that.accessories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessories);
    }

    @Override
    public String toString() {
        return "AccessoryNestContainerContents[" +
                "accessories=" + accessories + ']';
    }

}
