package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class CylinderBrush implements Brush {
    private final int size;
    private final int height;
    private final boolean hollow;

    public CylinderBrush(int size, int height, boolean hollow) {
        this.size = Math.max(1, size);
        this.height = Math.max(1, height);
        this.hollow = hollow;
    }

    @Override
    public void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern) {
        session.makeCylinder(target, pattern, size, size, height, !hollow);
    }

    @Override
    public String describe() { return (hollow ? "hollow " : "") + "cyl r=" + size + " h=" + height; }
}
