/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.monster.piglin.Piglin
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.equipment.trim.ArmorTrim
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 */
package com.ultra.megamod.feature.piglins;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid="megamod")
public class TrimmedArmoredPiglins {
    private static final String TRIM_APPLIED_TAG = "megamod:piglin_trim_applied";
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        float chance;
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Piglin)) {
            return;
        }
        Piglin piglin = (Piglin)entity;
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (piglin.getPersistentData().getBooleanOr(TRIM_APPLIED_TAG, false)) {
            return;
        }
        piglin.getPersistentData().putBoolean(TRIM_APPLIED_TAG, true);
        float f = chance = TrimmedArmoredPiglins.isInBastion(serverLevel, piglin) ? 0.16f : 0.08f;
        if (piglin.getRandom().nextFloat() >= chance) {
            return;
        }
        Registry patternRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        Registry materialRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
        List<Holder> patterns = patternRegistry.stream().map(arg_0 -> ((Registry)patternRegistry).wrapAsHolder(arg_0)).toList();
        List<Holder> materials = materialRegistry.stream().map(arg_0 -> ((Registry)materialRegistry).wrapAsHolder(arg_0)).toList();
        if (patterns.isEmpty() || materials.isEmpty()) {
            return;
        }
        Holder randomPattern = patterns.get(piglin.getRandom().nextInt(patterns.size()));
        Holder randomMaterial = materials.get(piglin.getRandom().nextInt(materials.size()));
        ArmorTrim trim = new ArmorTrim(randomMaterial, randomPattern);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorPiece = piglin.getItemBySlot(slot);
            if (armorPiece.isEmpty()) continue;
            armorPiece.set(DataComponents.TRIM, trim);
        }
    }

    private static boolean isInBastion(ServerLevel serverLevel, Piglin piglin) {
        return serverLevel.dimension() == Level.NETHER && piglin.getY() < 50.0;
    }
}

