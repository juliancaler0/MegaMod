package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ultra.megamod.feature.worldedit.history.ChangeSet;
import com.ultra.megamod.feature.worldedit.history.UndoHistory;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** Undo / redo / clearhistory. */
public final class HistoryCommands {
    private HistoryCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_undo")
            .executes(ctx -> undo(ctx.getSource(), 1))
            .then(Commands.argument("n", IntegerArgumentType.integer(1))
                .executes(ctx -> undo(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_redo")
            .executes(ctx -> redo(ctx.getSource(), 1))
            .then(Commands.argument("n", IntegerArgumentType.integer(1))
                .executes(ctx -> redo(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_clearhistory").executes(ctx -> clear(ctx.getSource())));
    }

    private static int undo(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        LocalSession ls = WECommandUtil.session(sp);
        UndoHistory h = ls.getHistory();
        int done = 0, blocks = 0;
        for (int i = 0; i < n; i++) {
            ChangeSet cs = h.popUndo();
            if (cs == null) break;
            blocks += cs.undo(sp.level());
            done++;
        }
        WECommandUtil.info(sp, "Undid " + done + " action(s), " + blocks + " block(s).");
        return 1;
    }

    private static int redo(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        LocalSession ls = WECommandUtil.session(sp);
        UndoHistory h = ls.getHistory();
        int done = 0, blocks = 0;
        for (int i = 0; i < n; i++) {
            ChangeSet cs = h.popRedo();
            if (cs == null) break;
            blocks += cs.redo(sp.level());
            done++;
        }
        WECommandUtil.info(sp, "Redid " + done + " action(s), " + blocks + " block(s).");
        return 1;
    }

    private static int clear(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        WECommandUtil.session(sp).getHistory().clear();
        WECommandUtil.info(sp, "History cleared.");
        return 1;
    }
}
