package com.ultra.megamod.feature.worldedit.command;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Cheap raycast helper used by the hpos-commands and brush tools. */
public final class RaycastHelper {
    private RaycastHelper() {}

    public static BlockPos block(ServerPlayer sp, double range) {
        Vec3 eye = sp.getEyePosition(1.0f);
        Vec3 look = sp.getViewVector(1.0f);
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
        BlockHitResult hr = sp.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, sp));
        return hr.getType() == HitResult.Type.BLOCK ? hr.getBlockPos().immutable() : null;
    }
}
