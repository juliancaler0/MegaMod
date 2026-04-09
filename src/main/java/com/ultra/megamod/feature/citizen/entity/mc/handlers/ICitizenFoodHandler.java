package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * Handler interface for citizen food tracking.
 * Ported from MineColonies' ICitizenFoodHandler.
 */
public interface ICitizenFoodHandler {

    /**
     * Food happiness stats.
     */
    record CitizenFoodStats(int diversity, int quality) {}

    /**
     * Add last eaten food item.
     */
    void addLastEaten(Item item);

    /**
     * Get the last eaten food item.
     */
    Item getLastEaten();

    /**
     * Check when we last ate a given food item.
     * -1 if not eaten recently.
     */
    int checkLastEaten(Item item);

    /**
     * Get the food happiness stats.
     */
    CitizenFoodStats getFoodHappinessStats();

    /**
     * Read from nbt.
     */
    void read(CompoundTag compound);

    /**
     * Write to nbt.
     */
    void write(CompoundTag compound);

    /**
     * Get the list of last eaten food items.
     */
    ImmutableList<Item> getLastEatenFoods();
}
