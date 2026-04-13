package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.WorldEditManager;
import com.ultra.megamod.feature.worldedit.WorldEditPermissions;
import com.ultra.megamod.feature.worldedit.history.ChangeSet;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Helpers that factor out repetitive checks for WE command implementations. */
public final class WECommandUtil {

    private WECommandUtil() {}

    public static ServerPlayer requireAdminPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sp = ctx.getSource().getPlayerOrException();
        if (!WorldEditPermissions.isAdmin(sp)) {
            ctx.getSource().sendFailure(Component.literal("WorldEdit is admin-only."));
            throw new CommandSyntaxException(
                new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                    Component.literal("Admin only")::getString),
                Component.literal("Admin only")::getString);
        }
        return sp;
    }

    public static LocalSession session(ServerPlayer sp) {
        return WorldEditManager.getSession(sp);
    }

    public static EditSession newEdit(ServerPlayer sp) {
        EditSession es = new EditSession(sp.level());
        LocalSession ls = session(sp);
        es.setMask(ls.getActiveMask());
        return es;
    }

    public static void finish(ServerPlayer sp, EditSession es, String desc) {
        if (es.getBlocksChanged() == 0) {
            sp.sendSystemMessage(Component.literal("No blocks changed.").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (es.isAborted()) {
            sp.sendSystemMessage(Component.literal("Edit aborted at limit of " + es.getMaxChanges()
                + " blocks — increase max-changed-blocks").withStyle(ChatFormatting.RED));
        }
        ChangeSet cs = es.getChangeSet();
        cs.setDescription(desc);
        session(sp).getHistory().record(cs);
        sp.sendSystemMessage(Component.literal("[WE] " + desc + ": " + es.getBlocksChanged() + " block(s)")
            .withStyle(ChatFormatting.GREEN));
    }

    public static void info(ServerPlayer sp, String msg) {
        sp.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.AQUA));
    }

    public static void error(ServerPlayer sp, String msg) {
        sp.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED));
    }

    public static int toSuccess(int count) { return Math.max(1, count); }
}
