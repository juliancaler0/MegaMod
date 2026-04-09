package com.ultra.megamod.feature.adminmodules.modules.player;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

import java.util.List;

public class AutoEatModule extends AdminModule {
    private ModuleSetting.IntSetting hungerThreshold;
    private ModuleSetting.EnumSetting fillMode;
    private ModuleSetting.EnumSetting mode;
    private ModuleSetting.DoubleSetting healthThreshold;
    private ModuleSetting.BoolSetting preferGoldenApple;
    private ModuleSetting.BoolSetting blacklistPoisonous;
    private int consumeCooldown = 0;

    public AutoEatModule() {
        super("auto_eat", "AutoEat", "Auto-restores hunger when low", ModuleCategory.PLAYER);
    }

    @Override
    protected void initSettings() {
        hungerThreshold = integer("Threshold", 14, 1, 19, "Hunger level to trigger");
        fillMode = enumVal("FillMode", "Both", List.of("Fill", "Saturate", "Both"), "Fill=food only, Saturate=saturation only, Both=both");
        mode = enumVal("Mode", "Direct", List.of("Direct", "Consume"), "Direct=god-mode fill, Consume=eat actual food from inventory");
        healthThreshold = decimal("HealthThreshold", 10.0, 1.0, 20.0, "Also trigger when health is below this");
        preferGoldenApple = bool("PreferGoldenApple", false, "Prefer golden apples when health is critical (<6)");
        blacklistPoisonous = bool("BlacklistPoisonous", true, "Don't eat poisonous food items");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Decrement cooldown
        if (consumeCooldown > 0) {
            consumeCooldown--;
        }

        boolean hungerTrigger = player.getFoodData().getFoodLevel() < hungerThreshold.getValue();
        boolean healthTrigger = player.getHealth() < healthThreshold.getValue();

        if (!hungerTrigger && !healthTrigger) return;

        if ("Direct".equals(mode.getValue())) {
            // Original god-mode behavior
            String m = fillMode.getValue();
            if ("Fill".equals(m) || "Both".equals(m)) {
                player.getFoodData().setFoodLevel(20);
            }
            if ("Saturate".equals(m) || "Both".equals(m)) {
                player.getFoodData().setSaturation(20.0f);
            }
        } else {
            // Consume mode: find and eat actual food from inventory
            if (consumeCooldown > 0) return; // Rate limit: 32 ticks between eats
            if (player.isUsingItem()) return; // Don't interrupt if already eating/using

            int bestSlot = -1;
            int bestNutrition = -1;
            boolean foundGolden = false;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (!stack.has(DataComponents.FOOD)) continue;

                // Blacklist check
                if (blacklistPoisonous.getValue() && isPoisonousFood(stack)) continue;

                FoodProperties food = stack.get(DataComponents.FOOD);
                if (food == null) continue;

                int nutrition = food.nutrition();

                // Prefer golden apple when health is critical
                if (preferGoldenApple.getValue() && player.getHealth() < 6.0f) {
                    boolean isGolden = stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
                    if (isGolden && !foundGolden) {
                        // First golden apple found, always pick it
                        bestSlot = i;
                        bestNutrition = nutrition;
                        foundGolden = true;
                        continue;
                    }
                    if (foundGolden && !isGolden) {
                        // Already found golden, skip non-golden
                        continue;
                    }
                }

                // Pick highest nutrition food
                if (nutrition > bestNutrition) {
                    bestNutrition = nutrition;
                    bestSlot = i;
                }
            }

            if (bestSlot >= 0) {
                ItemStack foodStack = player.getInventory().getItem(bestSlot);
                FoodProperties food = foodStack.get(DataComponents.FOOD);
                if (food != null) {
                    // Apply nutrition and saturation directly
                    int nutrition = food.nutrition();
                    float saturation = food.saturation();
                    int currentFood = player.getFoodData().getFoodLevel();
                    player.getFoodData().setFoodLevel(Math.min(20, currentFood + nutrition));
                    float currentSat = player.getFoodData().getSaturationLevel();
                    player.getFoodData().setSaturation(Math.min(currentFood + nutrition, currentSat + saturation));

                    // Apply food effects (e.g., golden apple gives absorption)
                    Consumable consumable = foodStack.get(DataComponents.CONSUMABLE);
                    if (consumable != null) {
                        for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
                            if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect statusEffect) {
                                if (player.getRandom().nextFloat() < statusEffect.probability()) {
                                    for (MobEffectInstance effectInstance : statusEffect.effects()) {
                                        player.addEffect(new MobEffectInstance(effectInstance));
                                    }
                                }
                            }
                        }
                    }

                    // Consume the item
                    foodStack.shrink(1);

                    // Rate limit: 32 ticks (1.6 seconds) to simulate eating time
                    consumeCooldown = 32;
                }
            }
        }
    }

    private boolean isPoisonousFood(ItemStack stack) {
        return stack.is(Items.POISONOUS_POTATO)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.PUFFERFISH)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.SUSPICIOUS_STEW);
    }
}
