package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** A brush applies an operation at the location the player is targeting. */
public interface Brush {
    void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern);
    String describe();
}
