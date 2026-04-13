package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class SphereBrush implements Brush {
    private final int size;
    private final boolean hollow;

    public SphereBrush(int size, boolean hollow) {
        this.size = Math.max(1, size);
        this.hollow = hollow;
    }

    @Override
    public void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern) {
        session.makeSphere(target, size, size, size, pattern, !hollow);
    }

    @Override
    public String describe() { return (hollow ? "hollow " : "") + "sphere r=" + size; }
}
