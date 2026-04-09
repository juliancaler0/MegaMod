/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package com.ultra.megamod.feature.relics.ability.hands;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class RageGloveAbility {
    private static final Identifier BARE_KNUCKLE_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "bare_knuckle_bonus");
    private static final Identifier FURY_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod","relic_rage_glove_fury");
    private static final Identifier BERSERK_DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod","relic_rage_glove_berserk_damage");
    private static final Identifier BERSERK_ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod","relic_rage_glove_berserk_armor");
    private static final String NBT_FURY_STACKS = "rage_glove_fury_stacks";
    private static final String NBT_FURY_LAST_HIT = "rage_glove_last_hit_time";
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Fury", "Attack speed stacks on consecutive hits", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("speed_per_stack", 0.05, 0.15, RelicStat.ScaleType.ADD, 0.01), new RelicStat("max_stacks", 3.0, 8.0, RelicStat.ScaleType.ADD, 1.0))), new RelicAbility("Bare Knuckle", "Bonus damage with empty main hand", 4, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("bonus_damage", 2.0, 6.0, RelicStat.ScaleType.ADD, 0.5))), new RelicAbility("Berserk", "Trade defense for increased damage", 7, RelicAbility.CastType.TOGGLE, List.of(new RelicStat("damage_bonus", 30.0, 60.0, RelicStat.ScaleType.ADD, 4.0), new RelicStat("armor_penalty", 20.0, 40.0, RelicStat.ScaleType.ADD, 3.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Rage Glove", "Fury", RageGloveAbility::executeFury);
        AbilityCastHandler.registerAbility("Rage Glove", "Bare Knuckle", RageGloveAbility::executeBareKnuckle);
        AbilityCastHandler.registerAbility("Rage Glove", "Berserk", RageGloveAbility::executeBerserk);
    }

    private static void executeFury(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        AttributeInstance attribute;
        if (player.tickCount % 10 != 0) {
            return;
        }
        double speedPerStack = stats[0];
        int maxStacks = (int)stats[1];
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        int furyStacks = tag.getIntOr(NBT_FURY_STACKS, 0);
        long lastHitTime = tag.getLongOr(NBT_FURY_LAST_HIT, 0L);
        long gameTime = player.level().getGameTime();
        if (gameTime - lastHitTime > 60L && furyStacks > 0) {
            furyStacks = Math.max(0, furyStacks - 1);
            tag.putInt(NBT_FURY_STACKS, furyStacks);
            tag.putLong(NBT_FURY_LAST_HIT, gameTime);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if ((attribute = player.getAttribute(Attributes.ATTACK_SPEED)) != null) {
            attribute.removeModifier(FURY_MODIFIER_ID);
            if (furyStacks > 0) {
                double bonus = speedPerStack * (double)Math.min(furyStacks, maxStacks);
                attribute.addTransientModifier(new AttributeModifier(FURY_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static void addFuryStack(ItemStack stack, ServerLevel level) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        int stacks = tag.getIntOr(NBT_FURY_STACKS, 0);
        double maxStacksStat = RelicData.getComputedStatValue(stack, "Fury", ABILITIES.get(0).stats().get(1));
        int maxStacks = (int)maxStacksStat;
        stacks = Math.min(stacks + 1, maxStacks);
        tag.putInt(NBT_FURY_STACKS, stacks);
        tag.putLong(NBT_FURY_LAST_HIT, level.getGameTime());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void executeBareKnuckle(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 10 != 0) {
            return;
        }
        double bonusDamage = stats[0];
        boolean emptyHand = player.getMainHandItem().isEmpty();
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) {
            return;
        }
        if (emptyHand) {
            attribute.removeModifier(BARE_KNUCKLE_MODIFIER_ID);
            attribute.addTransientModifier(new AttributeModifier(BARE_KNUCKLE_MODIFIER_ID, bonusDamage, AttributeModifier.Operation.ADD_VALUE));
            if (player.tickCount % 40 == 0) {
                ServerLevel level = (ServerLevel) player.level();
                level.sendParticles((ParticleOptions) ParticleTypes.CRIT, player.getX(), player.getY() + 0.8, player.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
            }
        } else {
            attribute.removeModifier(BARE_KNUCKLE_MODIFIER_ID);
        }
    }

    private static void executeBerserk(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        boolean active = AbilitySystem.isToggleActive(player.getUUID(), "Berserk");
        AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (active) {
            double damageBonus = stats[0] / 100.0;
            double armorPenalty = stats[1] / 100.0;
            if (damageAttr != null) {
                damageAttr.removeModifier(BERSERK_DAMAGE_MODIFIER_ID);
                damageAttr.addTransientModifier(new AttributeModifier(BERSERK_DAMAGE_MODIFIER_ID, damageBonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (armorAttr != null) {
                armorAttr.removeModifier(BERSERK_ARMOR_MODIFIER_ID);
                armorAttr.addTransientModifier(new AttributeModifier(BERSERK_ARMOR_MODIFIER_ID, -armorPenalty, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            if (damageAttr != null) {
                damageAttr.removeModifier(BERSERK_DAMAGE_MODIFIER_ID);
            }
            if (armorAttr != null) {
                armorAttr.removeModifier(BERSERK_ARMOR_MODIFIER_ID);
            }
        }
    }
}

