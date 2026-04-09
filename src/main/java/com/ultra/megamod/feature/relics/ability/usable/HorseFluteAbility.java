/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.ProblemReporter
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityProcessor
 *  net.minecraft.world.entity.EntitySpawnReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.animal.equine.AbstractHorse
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.TagValueOutput
 *  net.minecraft.world.level.storage.ValueOutput
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;

public class HorseFluteAbility {
    private static final String KEY_STORED_HORSE = "stored_horse";
    private static final String KEY_HORSE_NAME = "stored_horse_name";
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Store", "Capture your ridden horse into the item", 1, RelicAbility.CastType.INSTANTANEOUS, List.of()), new RelicAbility("Release", "Spawn your stored horse", 1, RelicAbility.CastType.INSTANTANEOUS, List.of()));

    public static void register() {
        AbilityCastHandler.registerAbility("Horse Flute", "Store", HorseFluteAbility::executeStore);
        AbilityCastHandler.registerAbility("Horse Flute", "Release", HorseFluteAbility::executeRelease);
    }

    private static void executeStore(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        if (tag.contains(KEY_STORED_HORSE)) {
            player.displayClientMessage(Component.literal("A horse is already stored! Release it first."), true);
            return;
        }
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractHorse)) {
            player.displayClientMessage(Component.literal("You must be riding a horse to store it!"), true);
            return;
        }
        AbstractHorse horse = (AbstractHorse)vehicle;
        player.stopRiding();
        ServerLevel level = (ServerLevel) player.level();
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        horse.save(valueOutput);
        CompoundTag horseData = valueOutput.buildResult();
        tag.put(KEY_STORED_HORSE, horseData);
        String horseName = horse.hasCustomName() ? horse.getCustomName().getString() : horse.getType().getDescription().getString();
        tag.putString(KEY_HORSE_NAME, horseName);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        horse.discard();
        player.displayClientMessage(Component.literal(("Stored " + horseName + " in the flute!")), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void executeRelease(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        if (!tag.contains(KEY_STORED_HORSE)) {
            player.displayClientMessage(Component.literal("No horse stored! Ride a horse and use Store first."), true);
            return;
        }
        CompoundTag horseData = tag.getCompoundOrEmpty(KEY_STORED_HORSE);
        String horseName = tag.getStringOr(KEY_HORSE_NAME, "Horse");
        ServerLevel level = (ServerLevel) player.level();
        Entity entity = EntityType.loadEntityRecursive(horseData, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (entity == null) {
            player.displayClientMessage(Component.literal("Failed to release horse!"), true);
            return;
        }
        entity.setPos(player.getX(), player.getY(), player.getZ());
        level.addFreshEntity(entity);
        tag.remove(KEY_STORED_HORSE);
        tag.remove(KEY_HORSE_NAME);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.displayClientMessage(Component.literal(("Released " + horseName + "!")), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}

