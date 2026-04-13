package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.clipboard.Clipboard;
import com.ultra.megamod.feature.worldedit.clipboard.ClipboardIO;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Clipboard operations: copy, cut, paste, flip, rotate.
 */
public final class ClipboardCommands {

    private ClipboardCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_copy").executes(ctx -> copy(ctx.getSource())));
        d.register(Commands.literal("we_cut").executes(ctx -> cut(ctx.getSource())));
        d.register(Commands.literal("we_paste")
            .executes(ctx -> paste(ctx.getSource(), true))
            .then(Commands.argument("skipAir", BoolArgumentType.bool())
                .executes(ctx -> paste(ctx.getSource(), BoolArgumentType.getBool(ctx, "skipAir")))));
        d.register(Commands.literal("we_rotate")
            .then(Commands.argument("deg", IntegerArgumentType.integer())
                .executes(ctx -> rotate(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "deg")))));
        d.register(Commands.literal("we_flip")
            .executes(ctx -> flip(ctx.getSource(), "x")));
        d.register(Commands.literal("we_clearclipboard").executes(ctx -> clear(ctx.getSource())));
    }

    private static int copy(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Clipboard cb = ClipboardIO.copyFromWorld(sp.level(), r, sp.blockPosition(), "clipboard");
        WECommandUtil.session(sp).setClipboard(cb);
        WECommandUtil.info(sp, "Copied " + cb.getVolume() + " block(s) to clipboard.");
        return 1;
    }

    private static int cut(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Clipboard cb = ClipboardIO.copyFromWorld(sp.level(), r, sp.blockPosition(), "clipboard");
        WECommandUtil.session(sp).setClipboard(cb);
        EditSession es = WECommandUtil.newEdit(sp);
        for (BlockPos p : r) es.setBlock(p, Blocks.AIR.defaultBlockState());
        WECommandUtil.finish(sp, es, "cut " + cb.getVolume());
        return 1;
    }

    private static int paste(CommandSourceStack src, boolean skipAir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Clipboard cb = WECommandUtil.session(sp).getClipboard();
        if (cb == null) { WECommandUtil.error(sp, "Clipboard is empty."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        int count = ClipboardIO.pasteToWorld(es, cb, sp.blockPosition(), skipAir);
        WECommandUtil.finish(sp, es, "paste " + count);
        return 1;
    }

    private static int rotate(CommandSourceStack src, int degrees) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Clipboard cb = WECommandUtil.session(sp).getClipboard();
        if (cb == null) { WECommandUtil.error(sp, "Clipboard is empty."); return 0; }
        Rotation rot = switch (((degrees % 360) + 360) % 360) {
            case 90 -> Rotation.CLOCKWISE_90;
            case 180 -> Rotation.CLOCKWISE_180;
            case 270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        Clipboard rotated = rotateClipboard(cb, rot);
        WECommandUtil.session(sp).setClipboard(rotated);
        WECommandUtil.info(sp, "Clipboard rotated " + degrees + " deg.");
        return 1;
    }

    private static int flip(CommandSourceStack src, String axis) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Clipboard cb = WECommandUtil.session(sp).getClipboard();
        if (cb == null) { WECommandUtil.error(sp, "Clipboard is empty."); return 0; }
        Mirror mirror = axis.equalsIgnoreCase("z") ? Mirror.FRONT_BACK : Mirror.LEFT_RIGHT;
        Clipboard flipped = mirrorClipboard(cb, mirror);
        WECommandUtil.session(sp).setClipboard(flipped);
        WECommandUtil.info(sp, "Clipboard flipped across " + axis);
        return 1;
    }

    private static int clear(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        WECommandUtil.session(sp).setClipboard(null);
        WECommandUtil.info(sp, "Clipboard cleared.");
        return 1;
    }

    private static Clipboard rotateClipboard(Clipboard cb, Rotation rot) {
        java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> out = new java.util.HashMap<>();
        var size = cb.getSize();
        for (var e : cb.getBlocks().entrySet()) {
            BlockPos rel = e.getKey();
            int x = rel.getX(), y = rel.getY(), z = rel.getZ();
            int nx, nz;
            switch (rot) {
                case CLOCKWISE_90 -> { nx = size.getZ() - 1 - z; nz = x; }
                case CLOCKWISE_180 -> { nx = size.getX() - 1 - x; nz = size.getZ() - 1 - z; }
                case COUNTERCLOCKWISE_90 -> { nx = z; nz = size.getX() - 1 - x; }
                default -> { nx = x; nz = z; }
            }
            out.put(new BlockPos(nx, y, nz), e.getValue().rotate(rot));
        }
        var newSize = (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90)
            ? new net.minecraft.core.Vec3i(size.getZ(), size.getY(), size.getX()) : size;
        return new Clipboard(newSize, out, new java.util.HashMap<>(), cb.getOrigin(), cb.getName());
    }

    private static Clipboard mirrorClipboard(Clipboard cb, Mirror mirror) {
        java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> out = new java.util.HashMap<>();
        var size = cb.getSize();
        for (var e : cb.getBlocks().entrySet()) {
            BlockPos rel = e.getKey();
            int x = rel.getX(), y = rel.getY(), z = rel.getZ();
            int nx = x, nz = z;
            switch (mirror) {
                case FRONT_BACK -> nx = size.getX() - 1 - x;
                case LEFT_RIGHT -> nz = size.getZ() - 1 - z;
                default -> {}
            }
            out.put(new BlockPos(nx, y, nz), e.getValue().mirror(mirror));
        }
        return new Clipboard(size, out, new java.util.HashMap<>(), cb.getOrigin(), cb.getName());
    }
}
