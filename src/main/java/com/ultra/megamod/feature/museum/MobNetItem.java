/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.ItemLike
 */
package com.ultra.megamod.feature.museum;

import com.ultra.megamod.feature.museum.MuseumRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;

public class MobNetItem
extends Item {
    public MobNetItem(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (target instanceof Player) {
            return InteractionResult.PASS;
        }
        ServerLevel level = (ServerLevel) player.level();
        EntityType<?> entityType = target.getType();
        String mobName = entityType.getDescription().getString();
        SpawnEggItem spawnEgg = SpawnEggItem.byId(entityType);
        ItemStack resultItem;
        if (spawnEgg != null) {
            resultItem = new ItemStack(spawnEgg);
        } else {
            resultItem = new ItemStack(MuseumRegistry.CAPTURED_MOB_ITEM.get());
            String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
            CustomData.update(DataComponents.CUSTOM_DATA, resultItem, tag -> {
                tag.putString("megamod_captured_mob", mobType);
                tag.putString("megamod_captured_mob_name", mobName);
            });
            resultItem.set(DataComponents.CUSTOM_NAME, Component.literal("Captured " + mobName).withStyle(ChatFormatting.AQUA));
        }
        target.discard();
        // When count is 1, set the hand directly to avoid Player.interactOn()
        // overwriting the result with EMPTY after seeing the shrunk stack is empty
        if (stack.getCount() == 1) {
            player.setItemInHand(hand, resultItem);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(resultItem)) {
                player.spawnAtLocation(level, resultItem);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.2f);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.literal("Captured " + mobName + "!").withStyle(ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
    }
}

