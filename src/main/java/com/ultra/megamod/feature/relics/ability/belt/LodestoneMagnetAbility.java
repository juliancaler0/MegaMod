package com.ultra.megamod.feature.relics.ability.belt;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LodestoneMagnetAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Attraction", "Pull nearby items toward you", 1,
            RelicAbility.CastType.PASSIVE,
            List.of(
                new RelicStat("range", 5.0, 8.0, RelicStat.ScaleType.ADD, 0.5),
                new RelicStat("speed", 0.1, 0.2, RelicStat.ScaleType.ADD, 0.015)
            )),
        new RelicAbility("Selective Pull", "Only attract items matching your inventory", 3,
            RelicAbility.CastType.TOGGLE,
            List.of(
                new RelicStat("range", 6.0, 10.0, RelicStat.ScaleType.ADD, 0.6)
            )),
        new RelicAbility("Expanded Range", "Greatly increased pull radius", 5,
            RelicAbility.CastType.PASSIVE,
            List.of(
                new RelicStat("bonus_range", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.6)
            )),
        new RelicAbility("Vacuum", "Instantly pull ALL items in a huge radius", 7,
            RelicAbility.CastType.INSTANTANEOUS,
            List.of(
                new RelicStat("vacuum_range", 20.0, 40.0, RelicStat.ScaleType.ADD, 3.0)
            ))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Lodestone Magnet", "Attraction", LodestoneMagnetAbility::executeAttraction);
        AbilityCastHandler.registerAbility("Lodestone Magnet", "Selective Pull", LodestoneMagnetAbility::executeSelectivePull);
        AbilityCastHandler.registerAbility("Lodestone Magnet", "Expanded Range", LodestoneMagnetAbility::executeExpandedRange);
        AbilityCastHandler.registerAbility("Lodestone Magnet", "Vacuum", LodestoneMagnetAbility::executeVacuum);
    }

    private static void executeAttraction(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 4 != 0) return;

        double range = stats[0];
        double speed = stats[1];
        pullItems(player, range, speed, false);
    }

    private static void executeSelectivePull(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 4 != 0) return;

        double range = stats[0];
        pullItems(player, range, 0.15, true);
    }

    private static void executeExpandedRange(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        // Passive stat bonus — the bonus_range is added to attraction range in pullItems
        // This ability doesn't need its own executor logic; it's read by the attraction handler
        // However, since the ability system calls each ability independently, we use this
        // to do a secondary pull at extended range with lower speed
        if (player.tickCount % 4 != 0) return;
        double bonusRange = stats[0];
        pullItems(player, bonusRange, 0.08, false);
    }

    private static void executeVacuum(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double vacuumRange = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(vacuumRange);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive());

        for (ItemEntity item : items) {
            if (item.hasPickUpDelay()) continue;
            item.teleportTo(player.getX(), player.getY(), player.getZ());
            item.setNoPickUpDelay();
        }
    }

    private static void pullItems(ServerPlayer player, double range, double speed, boolean selectiveOnly) {
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive());

        for (ItemEntity item : items) {
            if (item.hasPickUpDelay()) continue;

            if (selectiveOnly) {
                // Only pull items the player already has in their inventory
                boolean hasMatch = false;
                ItemStack droppedItem = item.getItem();
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invItem = player.getInventory().getItem(i);
                    if (!invItem.isEmpty() && ItemStack.isSameItemSameComponents(invItem, droppedItem)) {
                        hasMatch = true;
                        break;
                    }
                }
                if (!hasMatch) continue;
            }

            Vec3 direction = player.position().subtract(item.position()).normalize();
            double distance = item.distanceTo((Entity) player);
            // Scale speed inversely with distance for natural feel
            double adjustedSpeed = speed * Math.min(1.0, 3.0 / Math.max(distance, 0.5));
            item.setDeltaMovement(item.getDeltaMovement().add(direction.scale(adjustedSpeed)));
        }
    }
}
