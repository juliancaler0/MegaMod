/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

public class RollerSkatesAbility {
    private static final Identifier MOMENTUM_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "relic_roller_skates_momentum");
    private static final String NBT_SPRINT_TICKS = "roller_skates_sprint_ticks";
    private static final String NBT_DOUBLE_JUMP_USED = "roller_skates_double_jump";
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Momentum", "Speed increases while sprinting", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("max_speed", 0.02, 0.06, RelicStat.ScaleType.ADD, 0.005))), new RelicAbility("Double Jump", "Jump again while in the air", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("jump_power", 0.5, 1.0, RelicStat.ScaleType.ADD, 0.08))));

    public static void register() {
        AbilityCastHandler.registerAbility("Roller Skates", "Momentum", RollerSkatesAbility::executeMomentum);
        AbilityCastHandler.registerAbility("Roller Skates", "Double Jump", RollerSkatesAbility::executeDoubleJump);
    }

    private static void executeMomentum(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 5 != 0) {
            return;
        }
        double maxSpeed = stats[0];
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        int sprintTicks = tag.getIntOr(NBT_SPRINT_TICKS, 0);
        sprintTicks = player.isSprinting() ? Math.min(sprintTicks + 5, 100) : Math.max(sprintTicks - 10, 0);
        tag.putInt(NBT_SPRINT_TICKS, sprintTicks);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        double speedBonus = maxSpeed * ((double)sprintTicks / 100.0);
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MOMENTUM_MODIFIER_ID);
            if (speedBonus > 0.001) {
                attribute.addTransientModifier(new AttributeModifier(MOMENTUM_MODIFIER_ID, speedBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (player.onGround()) {
            CompoundTag djTag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
            djTag.putBoolean(NBT_DOUBLE_JUMP_USED, false);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(djTag));
        }
    }

    private static void executeDoubleJump(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double jumpPower = stats[0];
        if (player.onGround()) {
            return;
        }
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        boolean alreadyUsed = tag.getBooleanOr(NBT_DOUBLE_JUMP_USED, false);
        if (alreadyUsed) {
            return;
        }
        tag.putBoolean(NBT_DOUBLE_JUMP_USED, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, jumpPower, motion.z);
        player.hurtMarked = true;
        player.fallDistance = 0.0;
    }
}

