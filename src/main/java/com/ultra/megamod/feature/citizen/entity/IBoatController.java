package com.ultra.megamod.feature.citizen.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.Boat;

/**
 * Interface for citizens that can control boats.
 * Provides boat steering logic toward a target position.
 */
public interface IBoatController {

    BlockPos getSailPos();

    void setSailPos(BlockPos pos);

    default void updateBoatControl(Boat boat, BlockPos target) {
        if (target == null || boat == null) return;
        double dx = target.getX() + 0.5 - boat.getX();
        double dz = target.getZ() + 0.5 - boat.getZ();
        float targetYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        boat.setYRot(Mth.approachDegrees(boat.getYRot(), targetYaw, 5.0f));
        boat.setDeltaMovement(boat.getDeltaMovement().add(
            Mth.sin(-boat.getYRot() * Mth.DEG_TO_RAD) * 0.04, 0,
            Mth.cos(boat.getYRot() * Mth.DEG_TO_RAD) * 0.04));
    }
}
