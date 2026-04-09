package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles food tracking for the MC citizen.
 * Ported from MineColonies' CitizenFoodHandler concept.
 */
public class CitizenFoodHandler implements ICitizenFoodHandler {

    private static final int MAX_FOOD_HISTORY = 20;
    private static final String TAG_FOOD_HISTORY = "FoodHistory";

    private final Deque<Item> lastEaten = new ArrayDeque<>();

    @Override
    public void addLastEaten(Item item) {
        lastEaten.addFirst(item);
        while (lastEaten.size() > MAX_FOOD_HISTORY) {
            lastEaten.removeLast();
        }
    }

    @Override
    public Item getLastEaten() {
        return lastEaten.isEmpty() ? Items.AIR : lastEaten.peekFirst();
    }

    @Override
    public int checkLastEaten(Item item) {
        int index = 0;
        for (Item eaten : lastEaten) {
            if (eaten == item) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public CitizenFoodStats getFoodHappinessStats() {
        Set<Item> uniqueFoods = new HashSet<>(lastEaten);
        int diversity = uniqueFoods.size();
        // Quality: count how many different food types in last 10 items
        int quality = 0;
        int count = 0;
        Set<Item> recentUnique = new HashSet<>();
        for (Item item : lastEaten) {
            if (count >= 10) break;
            recentUnique.add(item);
            count++;
        }
        quality = recentUnique.size();
        return new CitizenFoodStats(diversity, quality);
    }

    @Override
    public void read(CompoundTag compound) {
        lastEaten.clear();
        if (compound.contains(TAG_FOOD_HISTORY)) {
            ListTag list = compound.getListOrEmpty(TAG_FOOD_HISTORY);
            for (int i = 0; i < list.size(); i++) {
                Tag tag = list.get(i);
                if (tag instanceof StringTag st) {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(st.value()));
                    if (item != null && item != Items.AIR) {
                        lastEaten.addLast(item);
                    }
                }
            }
        }
    }

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (Item item : lastEaten) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            list.add(StringTag.valueOf(id.toString()));
        }
        compound.put(TAG_FOOD_HISTORY, list);
    }

    @Override
    public ImmutableList<Item> getLastEatenFoods() {
        return ImmutableList.copyOf(lastEaten);
    }
}
