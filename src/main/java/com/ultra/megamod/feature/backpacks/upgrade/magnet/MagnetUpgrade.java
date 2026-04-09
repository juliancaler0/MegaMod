package com.ultra.megamod.feature.backpacks.upgrade.magnet;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import com.ultra.megamod.feature.backpacks.upgrade.ITickableUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Magnet upgrade — pulls nearby item entities toward the player.
 * Scans every 5 ticks for ItemEntity within 8 blocks and nudges them toward the player.
 */
public class MagnetUpgrade extends BackpackUpgrade implements ITickableUpgrade {

    private static final double RANGE = 8.0;
    private static final double PULL_SPEED = 0.4;

    @Override
    public String getId() {
        return "magnet";
    }

    @Override
    public String getDisplayName() {
        return "Magnet";
    }

    @Override
    public int getTickRate() {
        return 5;
    }

    @Override
    public void tick(ServerPlayer player, ServerLevel level) {
        if (!isActive()) return;

        Vec3 playerPos = player.position();
        AABB searchArea = new AABB(
                playerPos.x - RANGE, playerPos.y - RANGE, playerPos.z - RANGE,
                playerPos.x + RANGE, playerPos.y + RANGE, playerPos.z + RANGE
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchArea);
        for (ItemEntity itemEntity : items) {
            // Skip items with a pickup delay (just thrown, etc.)
            if (itemEntity.hasPickUpDelay()) continue;

            Vec3 direction = playerPos.subtract(itemEntity.position()).normalize().scale(PULL_SPEED);
            itemEntity.setDeltaMovement(direction);
        }
    }
}
