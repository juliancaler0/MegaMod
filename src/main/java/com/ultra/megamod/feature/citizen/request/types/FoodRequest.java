package com.ultra.megamod.feature.citizen.request.types;

import com.ultra.megamod.feature.citizen.request.IRequestable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Request for food items meeting a minimum nutrition threshold.
 * Matches any food item whose nutrition value is at least the specified minimum.
 */
public class FoodRequest implements IRequestable {

    private final int minNutrition;

    public FoodRequest(int minNutrition) {
        this.minNutrition = minNutrition;
    }

    public int getMinNutrition() {
        return minNutrition;
    }

    @Override
    public boolean matches(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return false;
        }
        return food.nutrition() >= minNutrition;
    }

    @Override
    public String getDescription() {
        return "Food (min nutrition: " + minNutrition + ")";
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "food");
        tag.putInt("minNutrition", minNutrition);
        return tag;
    }

    public static FoodRequest fromNbt(CompoundTag tag) {
        int minNutrition = tag.getIntOr("minNutrition", 1);
        return new FoodRequest(minNutrition);
    }
}
