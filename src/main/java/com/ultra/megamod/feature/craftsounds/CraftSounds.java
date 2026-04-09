/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemCraftedEvent
 */
package com.ultra.megamod.feature.craftsounds;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="megamod")
public class CraftSounds {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        float volume;
        SoundEvent sound;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel level = player2.level();
        ItemStack craftedStack = event.getCrafting();
        Item item = craftedStack.getItem();
        int stackSize = craftedStack.getCount();
        if (CraftSounds.isTool(craftedStack)) {
            sound = SoundEvents.ANVIL_USE;
            volume = 0.4f;
        } else if (CraftSounds.isArmor(craftedStack)) {
            sound = (SoundEvent)SoundEvents.ARMOR_EQUIP_IRON.value();
            volume = 0.7f;
        } else if (CraftSounds.isSpecialItem(item)) {
            sound = SoundEvents.ENCHANTMENT_TABLE_USE;
            volume = 0.8f;
        } else {
            sound = SoundEvents.WOOD_PLACE;
            volume = 0.6f;
        }
        float pitch = stackSize > 1 ? 1.2f : 1.0f;
        level.playSound(null, player2.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
        if (CraftSounds.isSpecialItem(item)) {
            level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player2.getX(), player2.getY() + 1.0, player2.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        } else {
            level.sendParticles((ParticleOptions)ParticleTypes.CRIT, player2.getX(), player2.getY() + 1.0, player2.getZ(), 6, 0.4, 0.4, 0.4, 0.1);
        }
    }

    private static boolean isTool(ItemStack stack) {
        return stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON);
    }

    private static boolean isArmor(ItemStack stack) {
        return stack.has(DataComponents.EQUIPPABLE);
    }

    private static boolean isSpecialItem(Item item) {
        return item == Items.BEACON || item == Items.ENCHANTING_TABLE || item == Items.END_CRYSTAL || item == Items.ENDER_CHEST;
    }
}

