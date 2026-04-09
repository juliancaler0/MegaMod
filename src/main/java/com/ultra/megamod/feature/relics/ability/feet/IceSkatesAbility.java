/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class IceSkatesAbility {
    private static final Identifier GLIDE_MODIFIER_ID = Identifier.fromNamespaceAndPath((String)"megamod", (String)"relic_ice_skates_glide");
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Glide", "Move faster on ice blocks", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("speed_bonus", 0.02, 0.06, RelicStat.ScaleType.ADD, 0.005))), new RelicAbility("Ice Trail", "Leave a trail of ice behind you", 4, RelicAbility.CastType.TOGGLE, List.of()));

    public static void register() {
        AbilityCastHandler.registerAbility("Ice Skates", "Glide", IceSkatesAbility::executeGlide);
        AbilityCastHandler.registerAbility("Ice Skates", "Ice Trail", IceSkatesAbility::executeIceTrail);
    }

    private static void executeGlide(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        boolean onIce;
        if (player.tickCount % 10 != 0) {
            return;
        }
        double speedBonus = stats[0];
        ServerLevel level = player.level();
        BlockPos belowPos = player.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        boolean bl = onIce = belowState.is(Blocks.ICE) || belowState.is(Blocks.PACKED_ICE) || belowState.is(Blocks.BLUE_ICE) || belowState.is(Blocks.FROSTED_ICE);
        if (onIce) {
            attribute.removeModifier(GLIDE_MODIFIER_ID);
            attribute.addTransientModifier(new AttributeModifier(GLIDE_MODIFIER_ID, speedBonus, AttributeModifier.Operation.ADD_VALUE));
        } else {
            attribute.removeModifier(GLIDE_MODIFIER_ID);
        }
    }

    private static void executeIceTrail(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Ice Trail")) {
            return;
        }
        ServerLevel level = player.level();
        BlockPos feetPos = player.blockPosition();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                BlockPos checkPos = feetPos.offset(dx, -1, dz);
                BlockState state = level.getBlockState(checkPos);
                if (!state.is(Blocks.WATER)) continue;
                level.setBlockAndUpdate(checkPos, Blocks.FROSTED_ICE.defaultBlockState());
            }
        }
    }
}

