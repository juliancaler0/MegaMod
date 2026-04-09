package com.ultra.megamod.feature.backpacks.upgrade.feeding;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import com.ultra.megamod.feature.backpacks.upgrade.ITickableUpgrade;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Feeding upgrade — automatically feeds the player from the backpack inventory
 * when their hunger drops below 18 (9 drumsticks).
 * Ticks every 20 game ticks (1 second).
 */
public class FeedingUpgrade extends BackpackUpgrade implements ITickableUpgrade {

    private static final int HUNGER_THRESHOLD = 18;

    private SimpleContainer backpackContainer;

    @Override
    public String getId() {
        return "feeding";
    }

    @Override
    public String getDisplayName() {
        return "Feeding";
    }

    @Override
    public int getTickRate() {
        return 20;
    }

    /**
     * Set the backpack's main container so the tick can scan it for food.
     */
    public void setBackpackContainer(SimpleContainer container) {
        this.backpackContainer = container;
    }

    @Override
    public void tick(ServerPlayer player, ServerLevel level) {
        if (!isActive()) return;
        if (backpackContainer == null) return;
        if (player.getFoodData().getFoodLevel() >= HUNGER_THRESHOLD) return;

        // Scan the container for the best food item
        int bestSlot = -1;
        int bestNutrition = 0;

        for (int i = 0; i < backpackContainer.getContainerSize(); i++) {
            ItemStack stack = backpackContainer.getItem(i);
            if (stack.isEmpty()) continue;
            if (!stack.has(DataComponents.FOOD)) continue;

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;

            int nutrition = food.nutrition();
            if (nutrition > bestNutrition) {
                bestNutrition = nutrition;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0) {
            ItemStack foodStack = backpackContainer.getItem(bestSlot);
            FoodProperties food = foodStack.get(DataComponents.FOOD);
            if (food != null) {
                int nutrition = food.nutrition();
                float saturation = food.saturation();

                int currentFood = player.getFoodData().getFoodLevel();
                player.getFoodData().setFoodLevel(Math.min(20, currentFood + nutrition));

                float currentSat = player.getFoodData().getSaturationLevel();
                player.getFoodData().setSaturation(Math.min(currentFood + nutrition, currentSat + saturation));

                foodStack.shrink(1);
                if (foodStack.isEmpty()) {
                    backpackContainer.setItem(bestSlot, ItemStack.EMPTY);
                }
            }
        }
    }
}
