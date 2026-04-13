package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** unstuck, ascend, descend, ceil, thru, up, jumpto. */
public final class NavigationCommands {
    private NavigationCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_unstuck").executes(ctx -> unstuck(ctx.getSource())));
        d.register(Commands.literal("we_ascend")
            .executes(ctx -> ascend(ctx.getSource(), 1))
            .then(Commands.argument("n", IntegerArgumentType.integer(1))
                .executes(ctx -> ascend(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_descend")
            .executes(ctx -> descend(ctx.getSource(), 1))
            .then(Commands.argument("n", IntegerArgumentType.integer(1))
                .executes(ctx -> descend(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_ceil")
            .executes(ctx -> ceil(ctx.getSource(), 1))
            .then(Commands.argument("n", IntegerArgumentType.integer(1))
                .executes(ctx -> ceil(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_thru").executes(ctx -> thru(ctx.getSource())));
        d.register(Commands.literal("we_up").then(Commands.argument("n", IntegerArgumentType.integer(1))
            .executes(ctx -> up(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        d.register(Commands.literal("we_jumpto").executes(ctx -> jumpto(ctx.getSource())));
    }

    private static int unstuck(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var lvl = sp.level();
        var at = sp.blockPosition();
        for (int y = at.getY(); y < lvl.getMaxY() - 2; y++) {
            if (lvl.getBlockState(at.atY(y)).isAir() && lvl.getBlockState(at.atY(y + 1)).isAir()) {
                sp.teleportTo(at.getX() + 0.5, y, at.getZ() + 0.5);
                return 1;
            }
        }
        return 0;
    }

    private static int ascend(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var lvl = sp.level();
        var at = sp.blockPosition();
        int jumped = 0;
        int y = at.getY();
        while (jumped < n && y < lvl.getMaxY() - 2) {
            y++;
            if (lvl.getBlockState(at.atY(y)).isAir() && lvl.getBlockState(at.atY(y + 1)).isAir()
                && !lvl.getBlockState(at.atY(y - 1)).isAir()) {
                jumped++;
                if (jumped >= n) { sp.teleportTo(at.getX() + 0.5, y, at.getZ() + 0.5); break; }
            }
        }
        return 1;
    }

    private static int descend(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var lvl = sp.level();
        var at = sp.blockPosition();
        int dropped = 0;
        int y = at.getY();
        while (dropped < n && y > lvl.getMinY() + 2) {
            y--;
            if (lvl.getBlockState(at.atY(y)).isAir() && lvl.getBlockState(at.atY(y + 1)).isAir()
                && !lvl.getBlockState(at.atY(y - 1)).isAir()) {
                dropped++;
                if (dropped >= n) { sp.teleportTo(at.getX() + 0.5, y, at.getZ() + 0.5); break; }
            }
        }
        return 1;
    }

    private static int ceil(CommandSourceStack src, int offset) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var lvl = sp.level();
        var at = sp.blockPosition();
        for (int y = at.getY() + 1; y < lvl.getMaxY(); y++) {
            if (!lvl.getBlockState(at.atY(y)).isAir()) {
                sp.teleportTo(at.getX() + 0.5, y - 2 - offset, at.getZ() + 0.5);
                return 1;
            }
        }
        return 0;
    }

    private static int thru(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Vec3 look = sp.getViewVector(1.0f);
        var cur = sp.position();
        sp.teleportTo(cur.x + look.x * 5, cur.y + look.y * 5, cur.z + look.z * 5);
        return 1;
    }

    private static int up(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        sp.teleportTo(sp.getX(), sp.getY() + n, sp.getZ());
        return 1;
    }

    private static int jumpto(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var target = RaycastHelper.block(sp, 300);
        if (target == null) return 0;
        sp.teleportTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);
        return 1;
    }
}
