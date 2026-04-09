package com.ultra.megamod.feature.relics.ability.belt;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class LeatherBeltAbility {
    private static final Identifier TOUGHNESS_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "relic_leather_belt_toughness");
    private static final Identifier ARMOR_TOUGHNESS_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "relic_leather_belt_armor_toughness");
    private static final Identifier CARRY_WEIGHT_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "carry_weight_bonus");
    private static final Identifier STEP_ASSIST_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "carry_weight_step_assist");

    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Toughness", "Increases maximum health and armor toughness", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("health_bonus", 4.0, 10.0, RelicStat.ScaleType.ADD, 1.0),
                    new RelicStat("armor_toughness", 1.0, 4.0, RelicStat.ScaleType.ADD, 0.5))),
            new RelicAbility("Endurance", "Reduces hunger drain and grants Resistance when hungry", 3,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("hunger_efficiency", 20.0, 50.0, RelicStat.ScaleType.ADD, 5.0))),
            new RelicAbility("Carry Weight", "Prevents slowdown from heavy armor and grants step assist", 5,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("weight_reduction", 40.0, 80.0, RelicStat.ScaleType.ADD, 8.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Leather Belt", "Toughness", LeatherBeltAbility::executeToughness);
        AbilityCastHandler.registerAbility("Leather Belt", "Endurance", LeatherBeltAbility::executeEndurance);
        AbilityCastHandler.registerAbility("Leather Belt", "Carry Weight", LeatherBeltAbility::executeCarryWeight);
    }

    private static void executeToughness(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        double healthBonus = stats[0];
        double armorToughnessBonus = stats[1];

        // Apply max health bonus
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(TOUGHNESS_MODIFIER_ID);
            healthAttribute.addTransientModifier(new AttributeModifier(TOUGHNESS_MODIFIER_ID, healthBonus, AttributeModifier.Operation.ADD_VALUE));
        }

        // Apply armor toughness bonus
        AttributeInstance armorToughnessAttribute = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughnessAttribute != null) {
            armorToughnessAttribute.removeModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
            armorToughnessAttribute.addTransientModifier(new AttributeModifier(ARMOR_TOUGHNESS_MODIFIER_ID, armorToughnessBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void executeEndurance(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) {
            return;
        }
        // Saturation effect
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 110, 0, false, false, true));

        // Grant Resistance I when below 50% food (food level < 10)
        if (player.getFoodData().getFoodLevel() < 10) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 110, 0, false, false, true));
        }
    }

    private static void executeCarryWeight(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        double weightReduction = stats[0];
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) {
            return;
        }
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        boolean wearingHeavyArmor = armorAttr != null && armorAttr.getValue() >= 10.0;
        if (wearingHeavyArmor) {
            // Enhanced speed bonus formula
            double speedBonus = 0.02 + (weightReduction / 50.0) * 0.03;
            speedAttr.removeModifier(CARRY_WEIGHT_MODIFIER_ID);
            speedAttr.addTransientModifier(new AttributeModifier(CARRY_WEIGHT_MODIFIER_ID, speedBonus, AttributeModifier.Operation.ADD_VALUE));

            // Grant Step Assist when wearing heavy armor
            AttributeInstance stepAttr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepAttr != null) {
                stepAttr.removeModifier(STEP_ASSIST_MODIFIER_ID);
                stepAttr.addTransientModifier(new AttributeModifier(STEP_ASSIST_MODIFIER_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
            }
        } else {
            speedAttr.removeModifier(CARRY_WEIGHT_MODIFIER_ID);
            // Remove step assist when not wearing heavy armor
            AttributeInstance stepAttr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepAttr != null) {
                stepAttr.removeModifier(STEP_ASSIST_MODIFIER_ID);
            }
        }
    }
}
