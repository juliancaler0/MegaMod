package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/** Chunk inspection commands. */
public final class ChunkCommands {
    private ChunkCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_chunkinfo").executes(ctx -> chunkInfo(ctx.getSource())));
        d.register(Commands.literal("we_listchunks").executes(ctx -> listChunks(ctx.getSource())));
    }

    private static int chunkInfo(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var cp = sp.chunkPosition();
        WECommandUtil.info(sp, "Chunk " + cp.x + "," + cp.z + "  mc: " + sp.level().dimension().identifier());
        return 1;
    }

    private static int listChunks(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        var mn = r.getMinimumPoint(); var mx = r.getMaximumPoint();
        java.util.Set<ChunkPos> set = new java.util.HashSet<>();
        for (int x = mn.getX() >> 4; x <= mx.getX() >> 4; x++)
            for (int z = mn.getZ() >> 4; z <= mx.getZ() >> 4; z++)
                set.add(new ChunkPos(x, z));
        WECommandUtil.info(sp, "Chunks in selection: " + set.size());
        int shown = 0;
        for (ChunkPos cp : set) {
            if (shown++ > 40) { sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("...")); break; }
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("  " + cp.x + "," + cp.z));
        }
        return 1;
    }
}
