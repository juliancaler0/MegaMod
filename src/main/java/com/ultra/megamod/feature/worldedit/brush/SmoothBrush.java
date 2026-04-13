package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/** Smooth/erosion brush. Simplified: replaces non-air cells with air
 *  when their neighborhood is under a threshold. */
public class SmoothBrush implements Brush {
    private final int size;
    private final int iterations;

    public SmoothBrush(int size, int iterations) {
        this.size = Math.max(1, size);
        this.iterations = Math.max(1, iterations);
    }

    @Override
    public void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern) {
        for (int it = 0; it < iterations; it++) {
            for (int x = -size; x <= size; x++) {
                for (int y = -size; y <= size; y++) {
                    for (int z = -size; z <= size; z++) {
                        if (x * x + y * y + z * z > size * size) continue;
                        BlockPos p = target.offset(x, y, z);
                        int solid = 0;
                        for (int dx = -1; dx <= 1; dx++)
                            for (int dy = -1; dy <= 1; dy++)
                                for (int dz = -1; dz <= 1; dz++) {
                                    if (!session.getLevel().getBlockState(p.offset(dx, dy, dz)).isAir()) solid++;
                                }
                        if (solid < 14) session.setBlock(p, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    @Override
    public String describe() { return "smooth r=" + size + " iter=" + iterations; }
}
