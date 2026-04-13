package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.region.CuboidRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class CuboidBrush implements Brush {
    private final int size;

    public CuboidBrush(int size) { this.size = Math.max(1, size); }

    @Override
    public void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern) {
        int half = size;
        CuboidRegion region = new CuboidRegion(
            target.offset(-half, -half, -half),
            target.offset(half, half, half)
        );
        session.set(region, pattern);
    }

    @Override
    public String describe() { return "cuboid r=" + size; }
}
