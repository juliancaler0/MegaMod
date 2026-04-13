package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.clipboard.Clipboard;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Pastes the session's clipboard at the brush target. */
public class ClipboardBrush implements Brush {
    private final Clipboard clipboard;
    private final boolean skipAir;

    public ClipboardBrush(Clipboard clipboard, boolean skipAir) {
        this.clipboard = clipboard;
        this.skipAir = skipAir;
    }

    @Override
    public void apply(EditSession session, ServerPlayer player, BlockPos target, Pattern pattern) {
        for (var entry : clipboard.getBlocks().entrySet()) {
            BlockPos rel = entry.getKey();
            var state = entry.getValue();
            if (skipAir && state.isAir()) continue;
            BlockPos worldPos = target.offset(rel.getX(), rel.getY(), rel.getZ());
            session.setBlock(worldPos, state);
        }
    }

    @Override
    public String describe() { return "clipboard brush (" + clipboard.getBlocks().size() + " blocks)"; }
}
