/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 */
package com.ultra.megamod.feature.totemvoid;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid="megamod")
public class TotemWorksInVoid {
    private static final double VOID_Y_THRESHOLD = -64.0;
    private static final int SAFE_TELEPORT_Y = 64;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }
        if (player.getY() > -64.0) {
            return;
        }
        InteractionHand totemHand = null;
        if (player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
            totemHand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            totemHand = InteractionHand.OFF_HAND;
        }
        if (totemHand == null) {
            return;
        }
        event.setNewDamage(0.0f);
        ItemStack totemStack = player.getItemInHand(totemHand);
        totemStack.shrink(1);
        ServerLevel level = (ServerLevel) player.level();
        BlockPos safePos = TotemWorksInVoid.findSafePosition(level, player);
        player.teleportTo((double)safePos.getX() + 0.5, (double)safePos.getY(), (double)safePos.getZ() + 0.5);
        player.fallDistance = 0.0;
        player.setDeltaMovement(0.0, 0.0, 0.0);
        if (player.getHealth() <= 0.0f) {
            player.setHealth(1.0f);
        }
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, true, true));
        player.connection.send(new ClientboundEntityEventPacket(player, (byte) 35));
    }

    private static BlockPos findSafePosition(ServerLevel level, ServerPlayer player) {
        int x = (int)Math.floor(player.getX());
        int z = (int)Math.floor(player.getZ());
        for (int y = 64; y >= -64; --y) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState below = level.getBlockState(checkPos);
            BlockState at = level.getBlockState(checkPos.above());
            BlockState above = level.getBlockState(checkPos.above(2));
            if (!below.isSolid() || at.isSolid() || above.isSolid()) continue;
            return checkPos.above();
        }
        return new BlockPos(x, 64, z);
    }
}

