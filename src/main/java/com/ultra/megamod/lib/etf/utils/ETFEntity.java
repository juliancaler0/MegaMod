package com.ultra.megamod.lib.etf.utils;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

/**
 * Interface mixed into entities / block entities to expose ETF-queryable data.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben). The {@code etf$} prefix is preserved
 * so future mixins applying this interface keep the upstream contract.
 */
public interface ETFEntity {

    boolean etf$canBeBright();

    boolean etf$isBlockEntity();

    @Nullable
    EntityType<?> etf$getType();

    UUID etf$getUuid();


    Level etf$getWorld();

    BlockPos etf$getBlockPos();

    int etf$getOptifineId();
    int etf$getOptifineVehicleId();

    int etf$getBlockY();

    CompoundTag etf$getNbt();

    boolean etf$hasCustomName();

    Component etf$getCustomName();

    Team etf$getScoreboardTeam();

    Iterable<ItemStack> etf$getItemsEquipped();

    Iterable<ItemStack> etf$getHandItems();

    Iterable<ItemStack> etf$getArmorItems();

    float etf$distanceTo(Entity entity);

    Vec3 etf$getVelocity();

    @Deprecated
    Pose etf$getPose();

    @Nullable
    String etf$getEntityKey();

}
