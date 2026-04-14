package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.BlockPattern;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.pattern.PatternParser;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One-shot preset commands that compose the lower-level region operations
 * into common building tasks (flatten, hill, clear, terraform, etc).
 *
 * Commands:
 *   /we_clear                          — set the entire selection to air
 *   /we_flatten [pattern]              — keep only the bottom layer of the selection,
 *                                        filled with `pattern` (default grass_block);
 *                                        everything above turns to air
 *   /we_hill [maxHeight] [pattern]     — sculpt a natural-looking hill inside the
 *                                        selection footprint (cosine falloff +
 *                                        per-column noise); surface = `pattern`
 *                                        (default grass_block), interior = dirt
 *   /we_terraform [pattern]            — shorthand: set everything to air, then
 *                                        overlay `pattern` on top of the floor
 *                                        (handy for clearing a build site)
 */
public final class PresetCommands {

    private PresetCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_clear").executes(ctx -> clear(ctx.getSource())));

        d.register(Commands.literal("we_flatten")
            .executes(ctx -> flatten(ctx.getSource(), "grass_block"))
            .then(Commands.argument("pattern", StringArgumentType.greedyString())
                .executes(ctx -> flatten(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));

        d.register(Commands.literal("we_hill")
            .executes(ctx -> hill(ctx.getSource(), 8, "grass_block"))
            .then(Commands.argument("h", IntegerArgumentType.integer(1, 256))
                .executes(ctx -> hill(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "h"), "grass_block"))
                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> hill(ctx.getSource(),
                        IntegerArgumentType.getInteger(ctx, "h"),
                        StringArgumentType.getString(ctx, "pattern"))))));

        d.register(Commands.literal("we_terraform")
            .executes(ctx -> terraform(ctx.getSource(), "grass_block"))
            .then(Commands.argument("pattern", StringArgumentType.greedyString())
                .executes(ctx -> terraform(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
    }

    private static int clear(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.set(r, new BlockPattern(Blocks.AIR.defaultBlockState()));
        WECommandUtil.finish(sp, es, "clear");
        return 1;
    }

    private static int flatten(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern surface = PatternParser.parse(patternStr);
        if (surface == null) { WECommandUtil.error(sp, "Invalid pattern: " + patternStr); return 0; }

        BlockPos min = r.getMinimumPoint();
        BlockPos max = r.getMaximumPoint();
        int floorY = min.getY();
        EditSession es = WECommandUtil.newEdit(sp);
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                m.set(x, floorY, z);
                if (!r.contains(m)) continue;
                es.setBlock(m.immutable(), surface);
                for (int y = floorY + 1; y <= max.getY(); y++) {
                    m.set(x, y, z);
                    if (r.contains(m)) es.setBlock(m.immutable(), air);
                }
            }
        }
        WECommandUtil.finish(sp, es, "flatten " + patternStr);
        return 1;
    }

    private static int hill(CommandSourceStack src, int maxHeight, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern surface = PatternParser.parse(patternStr);
        if (surface == null) { WECommandUtil.error(sp, "Invalid pattern: " + patternStr); return 0; }

        BlockPos min = r.getMinimumPoint();
        BlockPos max = r.getMaximumPoint();
        double cx = (min.getX() + max.getX()) * 0.5;
        double cz = (min.getZ() + max.getZ()) * 0.5;
        double radius = Math.max(1.0, Math.min(max.getX() - min.getX(), max.getZ() - min.getZ()) * 0.5);
        int floorY = min.getY();

        EditSession es = WECommandUtil.newEdit(sp);
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                m.set(x, floorY, z);
                if (!r.contains(m)) continue;
                double dx = x - cx, dz = z - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double norm = Math.min(1.0, dist / radius);
                // Cosine falloff: 1 at center, 0 at edge
                double smooth = 0.5 * (1.0 + Math.cos(norm * Math.PI));
                // Cheap pseudo-noise so it's not a perfect cone
                double noise = (Math.sin(x * 0.71) + Math.cos(z * 0.83) + Math.sin((x + z) * 0.41)) * 0.4;
                int columnH = Math.max(0, (int) Math.round(maxHeight * smooth + noise));
                int top = Math.min(max.getY(), floorY + columnH);
                // Dirt body
                for (int y = floorY; y < top; y++) {
                    m.set(x, y, z);
                    if (r.contains(m)) es.setBlock(m.immutable(), dirt);
                }
                // Surface cap
                m.set(x, top, z);
                if (r.contains(m)) es.setBlock(m.immutable(), surface);
                // Air above
                for (int y = top + 1; y <= max.getY(); y++) {
                    m.set(x, y, z);
                    if (r.contains(m)) es.setBlock(m.immutable(), air);
                }
            }
        }
        WECommandUtil.finish(sp, es, "hill h=" + maxHeight + " " + patternStr);
        return 1;
    }

    private static int terraform(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern surface = PatternParser.parse(patternStr);
        if (surface == null) { WECommandUtil.error(sp, "Invalid pattern: " + patternStr); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.set(r, new BlockPattern(Blocks.AIR.defaultBlockState()));
        es.overlay(r, surface);
        WECommandUtil.finish(sp, es, "terraform " + patternStr);
        return 1;
    }
}
